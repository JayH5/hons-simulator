package za.redbridge.simulator.config;

import java.awt.Paint;

import sim.util.Double2D;
import za.redbridge.simulator.interfaces.Phenotype;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.RobotObject;

public class HomogeneousRobotFactory implements RobotFactory {
    protected double mass;
    protected double radius;
    protected Paint paint;
    protected int width;
    protected int height;
    protected Phenotype phenotype;

    public HomogeneousRobotFactory(Phenotype phenotype, double mass, double radius, Paint paint, int width, int height) {
        this.mass = mass;
        this.radius = radius;
        this.paint = paint;
        this.width = width;
        this.height = height;
    }

    public RobotObject createInstance() {
        Double2D position = new Double2D((int)(Math.random()*width), (int)(Math.random()*height));
        return new RobotObject(phenotype, position, mass, radius, paint);
    }
}
