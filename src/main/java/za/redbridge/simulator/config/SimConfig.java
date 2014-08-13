package za.redbridge.simulator.config;

import sim.util.Int2D;

public class SimConfig {

    public enum Direction {

        NORTH, SOUTH, EAST, WEST;
    }

    protected long seed;
    protected Int2D envSize;
    protected int numRobots;

    protected Direction targetAreaPlacement;
    protected int targetAreaThickness;

    public SimConfig() {
        this.seed = System.currentTimeMillis();
        this.envSize = new Int2D(102,102);
        this.numRobots = 20;
    }

    public SimConfig(String filename) {
        throw new RuntimeException("TODO: Implement config reading from file");
    }

    public SimConfig(long seed, Int2D envSize, int numRobots, Direction targetAreaPlacement, int targetAreaThickness) {
        this.seed = seed;
        this.envSize = envSize;
        this.numRobots = numRobots;
        this.targetAreaPlacement = targetAreaPlacement;
        this.targetAreaThickness = targetAreaThickness;
    }

    public long getSeed() {
        return seed;
    }

    public Int2D getEnvSize() {
        return envSize;
    }

    public int getNumRobots() {
        return numRobots;
    }

    public Direction getTargetAreaPlacement() { return targetAreaPlacement; }

    public int getTargetAreaThickness() { return targetAreaThickness; }

}
