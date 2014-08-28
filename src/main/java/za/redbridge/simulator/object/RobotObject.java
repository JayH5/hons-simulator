package za.redbridge.simulator.object;

import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Drawable;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.PickupSensor;
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

    //how much force a wheel exerts when driven at full power
    private static final double WHEEL_POWER = 0.001;
    // The fraction of the robot's radius the wheels are away from the center
    private static final double WHEEL_DISTANCE = 0.75;

    private final Phenotype phenotype;
    private final CollisionSensor collisionSensor;
    private final PickupSensor pickupSensor;

    private final Vec2 leftWheelPosition;
    private final Vec2 rightWheelPosition;

    // Cached Vec2's for calculating wheel force and position of force
    private final Vec2 wheelForce = new Vec2();
    private final Vec2 wheelForcePosition = new Vec2();

    private boolean isBoundToResource = false;

    public RobotObject(World world, Double2D position, double radius, double mass, Paint paint,
                       Phenotype phenotype) {
        super(createPortrayal(radius, paint), createBody(world, position, radius, mass));
        this.phenotype = phenotype;

        collisionSensor = new CollisionSensor();
        // TODO: Make configurable or decide on good defaults
        pickupSensor = new PickupSensor(1f, 2f, 0f);
        initSensors();

        float wheelDistance = (float) (radius * WHEEL_DISTANCE);
        leftWheelPosition = new Vec2(0f, wheelDistance);
        rightWheelPosition = new Vec2(0f, -wheelDistance);
    }

    private void initSensors() {
        for (AgentSensor sensor : phenotype.getSensors()) {
            sensor.attach(this);
        }

        collisionSensor.attach(this);
        pickupSensor.attach(this);

        getPortrayal().setChildDrawable(new Drawable() {
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                for (Sensor sensor : phenotype.getSensors()) {
                    sensor.draw(object, graphics, info);
                }
                collisionSensor.draw(object, graphics, info);
                pickupSensor.draw(object, graphics, info);
            }

            @Override
            public void setOrientation(float orientation) {
                for (Sensor sensor : phenotype.getSensors()) {
                    sensor.getPortrayal().setOrientation(orientation);
                }
                collisionSensor.getPortrayal().setOrientation(orientation);
                pickupSensor.getPortrayal().setOrientation(orientation);
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
        List<SensorReading> readings = new ArrayList<>(sensors.size());
        sensors.forEach(sensor -> readings.add(sensor.sense()));

        Optional<Vec2> collision = collisionSensor.sense();
        Double2D wheelDrives = collision.map(o -> wheelDriveFromTargetPosition(o))
                .orElse(phenotype.step(readings));

        if (Math.abs(wheelDrives.x) > 1.0 || Math.abs(wheelDrives.y) > 1.0) {
            throw new RuntimeException("Invalid force applied: " + wheelDrives);
        }

        applyWheelForce(wheelDrives.x, leftWheelPosition);
        applyWheelForce(wheelDrives.y, rightWheelPosition);

        if (!isBoundToResource) {
            pickupSensor.sense().ifPresent(resource -> resource.tryPickup(this));
        }
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

    public boolean isBoundToResource() {
        return isBoundToResource;
    }

    public void setBoundToResource(boolean isBoundToResource) {
        this.isBoundToResource = isBoundToResource;
    }

    protected Double2D wheelDriveFromTargetPosition(Vec2 targetPos){
        double p2 = Math.PI / 2;

        //handle division by 0
        double angle = targetPos.x != 0.0 ? Math.atan(targetPos.y / targetPos.x) : p2;

        double a, b;
        //4 quadrants
        if(targetPos.x >= 0 && targetPos.y > 0){
            //first
            a = (p2 - angle) / p2;
            b = 1;
        }else if(targetPos.x < 0 && targetPos.y >= 0){
            //second
            a = -((p2 + angle) / p2);
            b = -1;
        }else if(targetPos.x <= 0 && targetPos.y < 0){
            //third
            a = -1;
            b = -((p2 - angle) / p2);
        }else if(targetPos.x > 0 && targetPos.y <= 0){
            //fourth
            a = 1;
            b = (p2 + angle) / p2;
        }else{
            throw new RuntimeException("wheelDriveFromTargetPosition quadrant check failed!");
        }
        return new Double2D(a, b);
    }

}
