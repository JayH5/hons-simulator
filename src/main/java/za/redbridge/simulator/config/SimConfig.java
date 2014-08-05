package za.redbridge.simulator.config;

import sim.util.Int2D;

public class SimConfig {
    protected long seed;
    protected Int2D envSize;
    protected int numRobots;

    public SimConfig() {
        this.seed = System.currentTimeMillis();
        this.envSize = new Int2D(102,102);
        this.numRobots = 20;
    }

    public SimConfig(String filename) {
        throw new RuntimeException("TODO: Implement config reading from file");
    }

    public SimConfig(long seed, Int2D envSize, int numRobots) {
        this.seed = seed;
        this.envSize = envSize;
        this.numRobots = numRobots;
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
}
