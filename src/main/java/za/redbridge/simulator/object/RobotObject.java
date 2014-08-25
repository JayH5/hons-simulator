package za.redbridge.simulator.object;

import org.jbox2d.common.Mat22;
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

    private static final double WHEEL_POWER = 0.005; //how much force a wheel exerts when driven at full power
    private final Phenotype phenotype;

    public RobotObject(World world, Double2D position, double radius, double mass, Paint paint,
                       Phenotype phenotype) {
        super(createPortrayal(radius, paint, phenotype), createBody(world, position, radius, mass));
        this.phenotype = phenotype;
        attachSensors();
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

        //Double2D wheelForces = phenotype.step(readings);
        Double2D wheelForces = new Double2D(1.0, 0.2);
        if(Math.abs(wheelForces.x) > 1.0 || Math.abs(wheelForces.y) > 1.0) {
            throw new RuntimeException("Invalid force applied: " + wheelForces);
        }
        //rotation matrix
        Mat22 rotM = Mat22.createRotationalTransform(getBody().getAngle());

        Vec2 wheel1Force = rotM.mul(new Vec2(0, (float)(wheelForces.x * WHEEL_POWER)));
        Vec2 wheel2Force = rotM.mul(new Vec2(0, (float)(wheelForces.y * WHEEL_POWER)));

        double radius = ((CirclePortrayal)getPortrayal()).getRadius();
        Vec2 position = getBody().getPosition();
        Vec2 wheel1Offset = rotM.mul(new Vec2((float)(0.75*radius),0));
        Vec2 wheel2Offset = rotM.mul(new Vec2((float)(-0.75*radius),0));

        Vec2 wheel1Position = position.add(wheel1Offset);
        Vec2 wheel2Position = position.add(wheel2Offset);

        getBody().applyForce(wheel1Force, wheel1Position);
        getBody().applyForce(wheel2Force, wheel2Position);
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
