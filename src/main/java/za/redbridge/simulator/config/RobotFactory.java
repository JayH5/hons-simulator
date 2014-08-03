package za.redbridge.simulator.config;

import java.awt.Paint;

import za.redbridge.simulator.object.RobotObject;

public class RobotFactory {
    protected double mass;
    protected double radius;
    protected Paint paint;
    protected PlacementPolicy placementPolicy;

    public RobotFactory(double mass, double radius, Paint paint, PlacementPolicy p) {
        this.mass = mass;
        this.radius = radius;
        this.paint = paint;
        this.placementPolicy = p;
    }

    public RobotObject createInstance() {
        return new RobotObject(placementPolicy.place(), mass, radius, paint);
    }
}
