package za.redbridge.simulator.config;

import za.redbridge.simulator.object.RobotObject;

public class RobotFactory {
    protected double mass;
    protected double radius;
    protected PlacementPolicy placementPolicy;

    public RobotFactory(double mass, double radius, PlacementPolicy p) {
        this.mass = mass;
        this.radius = radius;
        this.placementPolicy = p;
    }

    public RobotObject createInstance() {
        return new RobotObject(mass, radius, placementPolicy.place());
    }
}
