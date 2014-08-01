package za.redbridge.simulator.agent;

import sim.util.Double2D;
import za.redbridge.simulator.engine.SensorReading;
import za.redbridge.simulator.interfaces.Phenotype;
import za.redbridge.simulator.object.RobotObject;

import java.util.List;

public class Agent {
    protected Phenotype phenotype;
    protected RobotObject robot;

    public Agent(Phenotype p, RobotObject r) {
        this.phenotype = p;
        this.robot = r;
    }

    public Double2D step(List<SensorReading> l) {
        return phenotype.step(l);
    }
}
