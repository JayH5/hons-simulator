package za.redbridge.simulator.config;

import sim.util.Int2D;

public class SimConfig {
    protected long seed;
    protected Int2D envSize;

    public SimConfig() {
        this.seed = System.currentTimeMillis();
        this.envSize = new Int2D(102,102);
        //this.robotFactory = new HomogeneousRobotFactory(Phenotype.DUMMY_PHENOTYPE, 1.0, 2.0, new Color(0, 0, 255), envSize.getX(), envSize.getY());
    }

    public SimConfig(String filename) {
        throw new RuntimeException("TODO: Implement config reading from file");
    }

    public SimConfig(long seed, Int2D envSize) {
        this.seed = seed;
        this.envSize = envSize;
    }

    public long getSeed() {
        return seed;
    }

    public Int2D getEnvSize() {
        return envSize;
    }
}
