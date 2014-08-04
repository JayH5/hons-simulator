package za.redbridge.simulator.object;

import java.awt.Paint;

import sim.physics2D.shape.Circle;
import sim.physics2D.util.Angle;
import sim.util.Double2D;
import za.redbridge.simulator.interfaces.Phenotype;

/**
 * Object that represents a finished agent in the environment, including controller and all physical attributes.
 *
 * All RobotObjects are round with a fixed radius.
 *
 * Created by jamie on 2014/07/23.
 */
public class RobotObject extends PhysicalObject {
    Phenotype phenotype;

    public RobotObject(Phenotype phenotype, Double2D position, double mass, double radius, Paint paint) {
        this.phenotype = phenotype;
        setPose(position, new Angle(0));
        setShape(new Circle(radius, paint), mass);

        setCoefficientOfFriction(0);
        setCoefficientOfRestitution(1);
    }

}
