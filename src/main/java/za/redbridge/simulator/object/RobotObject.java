package za.redbridge.simulator.object;

import java.awt.*;

import sim.engine.SimState;
import sim.physics2D.shape.Circle;
import sim.physics2D.util.Angle;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.interfaces.Phenotype;
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
        setShape(new Circle(radius, paint), mass);

        setCoefficientOfFriction(0);
        setCoefficientOfRestitution(1);
    }

    @Override
    public void step(SimState sim) {
        super.step(sim);
        Sensor s = phenotype.getSensors().get(0);
        SensorReading r = s.sense(((Simulation)sim).getEnvironment(), this);
        double dist = r.getValues().get(0);
        this.getShape().setPaint(new Color((int)(dist*255),0,0));
    }
}
