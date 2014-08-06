package za.redbridge.simulator.object;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;

import sim.engine.SimState;
import sim.physics2D.shape.Rectangle;
import sim.physics2D.util.Angle;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.Sensor;
import za.redbridge.simulator.sensor.SensorReading;

/**
 * Object that represents a finished agent in the environment, including controller and all physical attributes.
 *
 * All RobotObjects are round with a fixed radius.
 *
 * Created by jamie on 2014/07/23.
 */
public class RobotObject extends MobileObject {
    Phenotype phenotype;

    public RobotObject(Phenotype phenotype, Double2D position, double mass, double radius, Paint paint) {
        this.phenotype = phenotype;
        setPose(position, new Angle(0));
        setShape(new RobotShape(radius, paint, phenotype), mass);

        //setCoefficientOfFriction(0.1);
        setCoefficientOfStaticFriction(0.4);
        setCoefficientOfRestitution(1);
    }

    @Override
    public void step(SimState sim) {
        super.step(sim);
        Sensor s = phenotype.getSensors().get(0);
        SensorReading r = s.sense(((Simulation)sim).getEnvironment(), this);
        double dist = r.getValues().get(0);
        this.getShape().setPaint(new Color((int)(dist*255),0,0));
        setAngularVelocity(getAngularVelocity() * 0.9);
        setVelocity(new Double2D(getVelocity().x * 1.005, getVelocity().y * 1.005));
    }

    private static class RobotShape extends Rectangle {

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
    }
}
