package za.redbridge.simulator.config;

import sim.util.Double2D;

public class RandomPlacementPolicy implements PlacementPolicy {
    protected double envWidth;
    protected double envHeight;

    public RandomPlacementPolicy(double w, double h) {
        envWidth = w;
        envHeight = h;
    }

    public Double2D place() {
        return new Double2D(Math.random() * envWidth, Math.random() * envHeight);
    }
}
