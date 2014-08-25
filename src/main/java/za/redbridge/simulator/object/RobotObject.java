package za.redbridge.simulator.object;

import org.jbox2d.common.Rot;
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
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;
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

    private final Vec2 leftWheelPosition;
    private final Vec2 rightWheelPosition;

    // Cached Vec2's for calculating wheel force and position of force
    private final Vec2 wheelForce = new Vec2();
    private final Vec2 wheelForcePosition = new Vec2();

    public RobotObject(World world, Double2D position, double radius, double mass, Paint paint,
                       Phenotype phenotype) {
        super(createPortrayal(radius, paint, phenotype), createBody(world, position, radius, mass));
        this.phenotype = phenotype;
        attachSensors();

        float wheelDistance = (float) (radius * WHEEL_DISTANCE);
        leftWheelPosition = new Vec2(0f, wheelDistance);
        rightWheelPosition = new Vec2(0f, -wheelDistance);
    }

    private void attachSensors() {
        for (Sensor sensor : phenotype.getSensors()) {
            sensor.attach(this);
        }
    }

    protected static Portrayal createPortrayal(double radius, Paint paint, Phenotype phenotype) {
        return new RobotPortrayal(radius, paint, phenotype);
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

        //setVelocity(new Double2D(getVelocity().x * 1.005, getVelocity().y * 1.005));

        List<Sensor> sensors = phenotype.getSensors();
        List<SensorReading> readings = new ArrayList<SensorReading>(sensors.size());
        for(Sensor sensor : sensors) {
            readings.add(sensor.sense());
        }

        // DEBUG
        Sensor sensor = phenotype.getSensors().get(0);
        SensorReading reading = sensor.sense();
        List<Double> values = reading.getValues();
        Double value = Collections.max(values);
        double dist = value != null ? value : 0;
        getPortrayal().setPaint(new Color((int) (dist * 255), 0, 0));

        //Double2D wheelDrives = phenotype.step(readings);
        Double2D wheelDrives = new Double2D(1.0, 0.2);
        if(Math.abs(wheelDrives.x) > 1.0 || Math.abs(wheelDrives.y) > 1.0) {
            throw new RuntimeException("Invalid force applied: " + wheelDrives);
        }

        applyWheelForce(wheelDrives.x, leftWheelPosition);
        applyWheelForce(wheelDrives.y, rightWheelPosition);
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

    private static class RobotPortrayal extends CirclePortrayal {

        final Phenotype phenotype;

        public RobotPortrayal(double radius, Paint paint, Phenotype phenotype) {
            super(radius, paint, true);
            this.phenotype = phenotype;
        }

        @Override
        protected void drawExtra(Object object, Graphics2D graphics, DrawInfo2D info) {
            // Draw all the sensors
            for (Sensor sensor : phenotype.getSensors()) {
                sensor.draw(graphics);
            }
        }
    }
}
