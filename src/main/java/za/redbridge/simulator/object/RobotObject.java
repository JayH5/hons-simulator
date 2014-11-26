package za.redbridge.simulator.object;

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import za.redbridge.simulator.phenotype.HeuristicPhenotype;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Drawable;
import za.redbridge.simulator.portrayal.PolygonPortrayal;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.sensor.AgentSensor;

/**
 * Object that represents a finished agent in the environment, including controller and all physical attributes.
 *
 * All RobotObjects are round with a fixed radius.
 *
 * Created by jamie on 2014/07/23.
 */
public class RobotObject extends PhysicalObject {

    private static final float WHEEL_RADIUS = 0.03f;

    private static final float ENGINE_TORQUE = (5 / 2f) * WHEEL_RADIUS;

    // The fraction of the robot's radius the wheels are away from the center
    private static final double WHEEL_DISTANCE = 0.75;

    private static final float MAX_LATERAL_IMPULSE = 1.0f;

    private static final float GROUND_TRACTION = 0.8f;
    private static final float VELOCITY_RAMPDOWN_START = 0.2f;
    private static final float VELOCITY_RAMPDOWN_END = 0.5f;

    private final Phenotype phenotype;
    private final HeuristicPhenotype heuristicPhenotype;

    private final Vec2 leftWheelPosition;
    private final Vec2 rightWheelPosition;

    // Cached Vec2's for calculating wheel force and position of force
    private final Vec2 wheelForce = new Vec2();
    private final Vec2 wheelForcePosition = new Vec2();

    private boolean isBoundToResource = false;

    private final Color defaultColor;

    private ArrayList<SpatialPoint> samplePoints;
    private ArrayList<Double> samplePolygonAreas;

    private final Portrayal directionPortrayal = new DirectionPortrayal();

    public RobotObject(World world, Vec2 position, float angle, double radius, double mass,
            Color color, Phenotype phenotype, Vec2 targetAreaPosition) {
        super(createPortrayal(radius, color), createBody(world, position, angle, radius, mass));

        this.phenotype = phenotype;
        this.defaultColor = color;
        directionPortrayal.setPaint(invertColor(color));

        heuristicPhenotype = new HeuristicPhenotype(phenotype, this, targetAreaPosition);
        initSensors();

        float wheelDistance = (float) (radius * WHEEL_DISTANCE);
        leftWheelPosition = new Vec2(0f, wheelDistance);
        rightWheelPosition = new Vec2(0f, -wheelDistance);

        samplePoints = new ArrayList<>();
        samplePolygonAreas = new ArrayList<>();
    }

    private void initSensors() {
        for (AgentSensor sensor : phenotype.getSensors()) {
            sensor.attach(this);
        }

        getPortrayal().setChildDrawable(new Drawable() {
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                directionPortrayal.draw(object, graphics, info);
                heuristicPhenotype.draw(object, graphics, info);
                for (AgentSensor sensor : phenotype.getSensors()) {
                    Portrayal portrayal = sensor.getPortrayal();
                    if (portrayal != null) {
                        portrayal.draw(object, graphics, info);
                    }
                }
            }

            @Override
            public void setTransform(Transform transform) {
                directionPortrayal.setTransform(transform);
                heuristicPhenotype.setTransform(transform);
                for (AgentSensor sensor : phenotype.getSensors()) {
                    Portrayal portrayal = sensor.getPortrayal();
                    if (portrayal != null) {
                        portrayal.setTransform(transform);
                    }
                }
            }
        });
    }

    protected static Portrayal createPortrayal(double radius, Paint paint) {
        return new CirclePortrayal(radius, paint, true);
    }

    protected static Body createBody(World world, Vec2 position, float angle, double radius,
            double mass) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.DYNAMIC)
                .setPosition(position)
                .setAngle(angle)
                .setCircular(radius, mass)
                .setFriction(0.7f)
                .setRestitution(1.0f)
                .setGroundFriction(0.3f, 0.2f, 0.04f, 0.06f)
                .setFilterCategoryBits(FilterConstants.CategoryBits.ROBOT)
                .build(world);
    }

    public float getRadius() {
        return (float) ((CirclePortrayal) getPortrayal()).getRadius();
    }

    @Override
    public void step(SimState sim) {
        super.step(sim);

        List<AgentSensor> sensors = phenotype.getSensors();
        List<List<Double>> readings = new ArrayList<>(sensors.size());
        for (AgentSensor sensor : sensors) {
            readings.add(sensor.sense());
        }

        Double2D wheelDrives = heuristicPhenotype.step(readings);

        if (Math.abs(wheelDrives.x) > 1.0 || Math.abs(wheelDrives.y) > 1.0) {
            throw new RuntimeException("Invalid force applied: " + wheelDrives);
        }

        applyWheelDrive((float) wheelDrives.x, leftWheelPosition);
        applyWheelDrive((float) wheelDrives.y, rightWheelPosition);

        updateFriction();

        if (sim.schedule.getSteps() % 50 == 0 && !heuristicPhenotype.getActiveHeuristic().equalsIgnoreCase("none")) {

            SpatialPoint sample = new SpatialPoint(this.getBody().getPosition(), samplePoints);
            samplePoints.add(sample);
            //after collecting 4 points (or something), calculate area and flush sample point buffer
            if (samplePoints.size() == 4) {

                Collections.sort(samplePoints);
                samplePolygonAreas.add(calculatePolygonArea(samplePoints));
                samplePoints.clear();
            }
        }
    }

    private void applyWheelDrive(float wheelDrive, Vec2 wheelPosition) {
        final Body body = getBody();

        // Calculate the force due to the wheel
        Vec2 velocity = body.getLinearVelocity();//body.getWorldVector(body.getLinearVelocity());
        float speed = velocity.length();
        float velocityInWheelDirection = speed * velocity.x / speed;

        // if the robot velocity is in the opposite direction of wheel drive direction, our torque
        // output is not constrained
        if (Math.signum(velocityInWheelDirection) != Math.signum(wheelDrive)) {
            velocityInWheelDirection = 0.0f;
        }
        float magnitude = (wheelDrive * torqueAtVelocity(velocityInWheelDirection)) / WHEEL_RADIUS;
        wheelForce.set(magnitude, 0f);
        body.getWorldVectorToOut(wheelForce, wheelForce);

        // Calculate position of force
        body.getWorldPointToOut(wheelPosition, wheelForcePosition);

        // Apply force
        body.applyForce(wheelForce, wheelForcePosition);
    }

    /**
     * @param velocity The robot velocity in the direction of the intended wheel travel.
     * @return The torque applied to the wheels by the engine at the given velocity.
     */
    private float torqueAtVelocity(float velocity) {
        velocity = Math.abs(velocity);
        if (velocity < VELOCITY_RAMPDOWN_START) {
            return ENGINE_TORQUE;
        } else if (velocity > VELOCITY_RAMPDOWN_END) {
            return 0;
        } else {
            return ENGINE_TORQUE * (1.0f - ((velocity - VELOCITY_RAMPDOWN_START)
                    / (VELOCITY_RAMPDOWN_END - VELOCITY_RAMPDOWN_START)));
        }
    }

    /**
     * For the below 2 methods, see: http://www.iforce2d.net/src/iforce2d_TopdownCar.h
     */
    private Vec2 getLateralVelocity() {
        Vec2 currentRightNormal = getBody().getWorldVector(new Vec2(1, 0));
        currentRightNormal.mulLocal(Vec2.dot(currentRightNormal, getBody().getLinearVelocity()));
        return currentRightNormal;
    }

    private void updateFriction() {
        Vec2 impulse = getLateralVelocity()
                .negateLocal()
                .mulLocal(getBody().getMass());

        float impulseMagnitude = impulse.length();
        if (impulseMagnitude > MAX_LATERAL_IMPULSE) {
            impulse.mulLocal(MAX_LATERAL_IMPULSE / impulseMagnitude);
        }

        getBody().applyLinearImpulse(impulse.mulLocal(GROUND_TRACTION), getBody().getWorldCenter(),
                false);
        getBody().applyAngularImpulse(GROUND_TRACTION * 0.1f * getBody().getInertia()
                * -getBody().getAngularVelocity());
     }

    public boolean isBoundToResource() {
        return isBoundToResource;
    }

    public HeuristicPhenotype getHeuristicPhenotype() { return heuristicPhenotype; }

    public double getAverageCoveragePolgygonArea() {

        double sum = 0;
        for (Double area: samplePolygonAreas) {
            sum += area;
        }

        return sum/samplePolygonAreas.size();
    }

    public void setBoundToResource(boolean isBoundToResource) {
        this.isBoundToResource = isBoundToResource;
    }

    public void setColor(Color color) {
        if (color == null) {
            color = defaultColor;
        }

        getPortrayal().setPaint(color);
        directionPortrayal.setPaint(invertColor(color));
    }

    private static Color invertColor(Color color) {
        return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
    }

    public Phenotype getPhenotype() {
        return phenotype;
    }

    private static class DirectionPortrayal extends PolygonPortrayal {

        static final float WIDTH = 0.1f;
        static final float HEIGHT = 0.2f;
        static final float THICKNESS = 0.1f;

        DirectionPortrayal() {
            super(6);

            final float halfWidth = WIDTH / 2;
            final float halfHeight = HEIGHT / 2;
            final float halfThickness = THICKNESS / 2;
            vertices[0].set(-halfWidth + halfThickness, halfHeight);
            vertices[1].set(halfWidth + halfThickness, 0f);
            vertices[2].set(-halfWidth + halfThickness, -halfHeight);
            vertices[3].set(-halfWidth - halfThickness, -halfHeight);
            vertices[4].set(halfWidth - halfThickness, 0f);
            vertices[5].set(-halfWidth - halfThickness, halfHeight);
        }
    }

    //class for spatial point in the context of some shape
    private static class SpatialPoint implements Comparable<SpatialPoint> {

        private final Vec2 point;
        private final ArrayList<SpatialPoint> otherPoints;

        public SpatialPoint(Vec2 point, ArrayList<SpatialPoint> otherPoints) {
            this.point = point;
            this.otherPoints = otherPoints;
        }

        public int compareTo(SpatialPoint other) {

            Vec2 center = getCenterOfPoints(otherPoints);

            if (this.point.x >= 0 && other.point.x < 0) {
                return 1;
            }
            else if (this.point.x == 0 && other.point.x ==0) {
                return Float.compare(this.point.y,other.point.y);
            }

            float delta = (this.point.x - center.x) * (other.point.y - center.y)
                    - (this.point.x - center.x) * (this.point.y - center.y);

            if (delta > 0) {
                return 1;
            }
            else if (delta < 0) {
                return -1;
            }

            float distFromCenter1 = (this.point.x - center.x) * (this.point.x - center.x) + (this.point.y - center.y) * (this.point.y - center.y);
            float distFromCenter2 = (other.point.x - center.x) * (other.point.x - center.x) + (other.point.y - center.y) * (other.point.y - center.y);

            return Float.compare(distFromCenter1,distFromCenter2);

        }

        private static Vec2 getCenterOfPoints(ArrayList<SpatialPoint> points) {

            final Vec2 center = new Vec2(0,0);
            for (SpatialPoint point: points) {

                center.x += point.getPoint().x;
                center.y += point.getPoint().y;
            }
            center.x /= points.size();
            center.y /= points.size();
            return center;
        }

        public Vec2 getPoint() { return point; }
    }

    private double calculatePolygonArea(ArrayList<SpatialPoint> vertices) {

        double totalArea = 0;
        int j = vertices.size()-1;

        for (int i = 0; i < vertices.size(); i++) {
            totalArea +=  (vertices.get(j).getPoint().x + vertices.get(i).getPoint().x) *
                    (vertices.get(j).getPoint().y + vertices.get(i).getPoint().y);
            j = i;
        }
        return totalArea/2;
    }


}
