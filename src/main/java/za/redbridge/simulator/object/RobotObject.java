package za.redbridge.simulator.object;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.List;

import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.sensor.Sensor;
import za.redbridge.simulator.sensor.SensorReading;


import static za.redbridge.simulator.Utils.toVec2;

/**
 * Object that represents a finished agent in the environment, including controller and all physical attributes.
 *
 * All RobotObjects are round with a fixed radius.
 *
 * Created by jamie on 2014/07/23.
 */
public class RobotObject extends PhysicalObject {

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
                .setPosition(toVec2(position))
                .setCircular((float) radius)
                .setDensity((float) (mass / (2 * Math.PI * radius * radius)))
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

        Sensor sensor = phenotype.getSensors().get(0);
        SensorReading reading = sensor.sense();
        List<Double> values = reading.getValues();
        double dist = !values.isEmpty() ? reading.getValues().get(0) : 0;

        getPortrayal().setPaint(new Color((int) (dist * 255), 0, 0));
        //setVelocity(new Double2D(getVelocity().x * 1.005, getVelocity().y * 1.005));
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
