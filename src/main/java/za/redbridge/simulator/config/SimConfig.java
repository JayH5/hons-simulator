package za.redbridge.simulator.config;

import sim.util.Double2D;
import za.redbridge.simulator.agent.Agent;
import za.redbridge.simulator.interfaces.Phenotype;
import za.redbridge.simulator.object.RobotObject;

public class SimConfig {
    protected long seed;
    protected Double2D envSize;
    protected RobotFactory robotFactory;

    public SimConfig(long seed, Double2D envSize, RobotFactory robotFactory) {
        this.seed = seed;
        this.envSize = envSize;
        this.robotFactory = robotFactory;
    }

    public long getSeed() {
        return seed;
    }

    public Double2D getEnvSize() {
        return envSize;
    }

    public Agent createAgent(Phenotype p) {
        RobotObject r = robotFactory.createInstance();
        return new Agent(p, r);
    }
}
