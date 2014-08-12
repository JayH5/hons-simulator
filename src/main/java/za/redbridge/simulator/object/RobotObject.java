package za.redbridge.simulator.object;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.awt.Paint;

import sim.engine.SimState;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;


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
        super(createPortrayal(radius, paint), createBody(world, position, radius, mass));
        this.phenotype = phenotype;
    }

    protected static Portrayal createPortrayal(double radius, Paint paint) {
        return new CirclePortrayal(radius, paint, true);
    }

    protected static Body createBody(World world, Double2D position, double radius, double mass) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.DYNAMIC)
                .setPosition(toVec2(position))
                .setCircular((float) radius)
                .setDensity((float) (mass / (2 * Math.PI * radius * radius)))
                .setFriction(0f)
                .setRestitution(1.0f)
                .build(world);
    }

    @Override
    public void step(SimState sim) {
        super.step(sim);

        /*Sensor sensor = phenotype.getSensors().get(0);
        SensorReading reading = sensor.sense(((Simulation)sim).getEnvironment(), this);
        double dist = reading.getValues().get(0);

        this.getShape().setPaint(new Color((int) (dist*255),0,0));
        setAngularVelocity(getAngularVelocity() * 0.9);
        setVelocity(new Double2D(getVelocity().x * 1.005, getVelocity().y * 1.005));*/

        Simulation simulation = (Simulation) sim;
        
    }

    /*private static class RobotShape extends Rectangle {

        final Phenotype phenotype;

        public RobotShape(double radius, Paint paint, Phenotype phenotype) {
            super(radius * 2, radius * 2, paint);
            this.phenotype = phenotype;
        }

        @Override
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            super.draw(object, graphics, info);

            Angle orientation = getOrientation();
            // Draw all the sensors
            for (Sensor sensor : phenotype.getSensors()) {
                sensor.draw(object, graphics, info, orientation);
            }
        }
    }*/
}
