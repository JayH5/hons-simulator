package za.redbridge.simulator.object;

import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.List;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.phenotype.HeuristicPhenotype;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Drawable;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.Sensor;
import za.redbridge.simulator.sensor.SensorReading;

/**
 * Object that represents a finished agent in the environment, including controller and all physical attributes.
 *
 * All RobotObjects are round with a fixed radius.
 *
 * Created by jamie on 2014/07/23.
 */
public class RobotObject extends PhysicalObject {

    //how much force a wheel exerts when driven at full power, in newtons
    private static final double WHEEL_POWER = 0.15;
    // The fraction of the robot's radius the wheels are away from the center
    private static final double WHEEL_DISTANCE = 0.75;

    private static final float MAX_LATERAL_IMPULSE = 1.0f;

    private static final float GROUND_TRACTION = 0.8f;

    private final Phenotype phenotype;
    private final HeuristicPhenotype heuristicPhenotype;

    private final Vec2 leftWheelPosition;
    private final Vec2 rightWheelPosition;

    // Cached Vec2's for calculating wheel force and position of force
    private final Vec2 wheelForce = new Vec2();
    private final Vec2 wheelForcePosition = new Vec2();

    private boolean isBoundToResource = false;

    public RobotObject(World world, Double2D position, double radius, double mass, Paint paint,
                        Phenotype phenotype, SimConfig.Direction targetAreaPlacement) {

        super(createPortrayal(radius, paint), createBody(world, position, radius, mass));
        this.phenotype = phenotype;
        heuristicPhenotype = new HeuristicPhenotype(phenotype, this, targetAreaPlacement);
        initSensors();

        float wheelDistance = (float) (radius * WHEEL_DISTANCE);
        leftWheelPosition = new Vec2(0f, wheelDistance);
        rightWheelPosition = new Vec2(0f, -wheelDistance);
    }

    private void initSensors() {
        for (AgentSensor sensor : phenotype.getSensors()) {
            sensor.attach(this);
        }

        getPortrayal().setChildDrawable(new Drawable() {
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                for (Sensor sensor : phenotype.getSensors()) {
                    sensor.draw(object, graphics, info);
                }
                heuristicPhenotype.getCollisionSensor().draw(object, graphics, info);
                heuristicPhenotype.getPickupSensor().draw(object, graphics, info);
            }

            @Override
            public void setTransform(Transform transform) {
                for (Sensor sensor : phenotype.getSensors()) {
                    sensor.getPortrayal().setTransform(transform);
                }
                heuristicPhenotype.getCollisionSensor().getPortrayal().setTransform(transform);
                heuristicPhenotype.getPickupSensor().getPortrayal().setTransform(transform);
            }
        });
    }

    protected static Portrayal createPortrayal(double radius, Paint paint) {
        return new CirclePortrayal(radius, paint, true);
    }

    protected static Body createBody(World world, Double2D position, double radius, double mass) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.DYNAMIC)
                .setPosition(position)
                .setCircular(radius, mass)
                .setFriction(0.7f)
                .setRestitution(1.0f)
                .build(world);
    }

    public float getRadius() {
        return (float) ((CirclePortrayal) getPortrayal()).getRadius();
    }

    @Override
    public void step(SimState sim) {
        super.step(sim);

        List<AgentSensor> sensors = phenotype.getSensors();
        List<SensorReading> readings = sensors.stream().map(s -> s.sense())
                .collect(Collectors.toList());

        for(int i = 0; i < readings.size(); i++){
            if(readings.get(i).getValues().get(0).isNaN()) {
                System.out.println("LOOOOL");
            }
        }
        Double2D wheelDrives = heuristicPhenotype.step(readings);

        if (Math.abs(wheelDrives.x) > 1.0 || Math.abs(wheelDrives.y) > 1.0) {
            throw new RuntimeException("Invalid force applied: " + wheelDrives);
        }

        applyWheelForce(wheelDrives.x, leftWheelPosition);
        applyWheelForce(wheelDrives.y, rightWheelPosition);

        updateFriction();
    }

    private void applyWheelForce(double wheelDrive, Vec2 wheelPosition) {
        final Transform bodyTransform = getBody().getTransform();

        // Calculate the force due to the wheel
        float magnitude = (float) (wheelDrive * WHEEL_POWER);
        wheelForce.set(magnitude, 0f);
        Rot.mulToOut(bodyTransform.q, wheelForce, wheelForce);

        // Calculate position of force
        Transform.mulToOut(bodyTransform, wheelPosition, wheelForcePosition);

        // Apply force
        getBody().applyForce(wheelForce, wheelForcePosition);
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

    public void setBoundToResource(boolean isBoundToResource) {
        this.isBoundToResource = isBoundToResource;
    }
}
