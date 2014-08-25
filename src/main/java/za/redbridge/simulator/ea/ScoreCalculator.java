package za.redbridge.simulator.ea;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;

import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.interfaces.ResourceFactory;
import za.redbridge.simulator.interfaces.RobotFactory;

/**
 * Created by shsu on 2014/08/13.
 */

public class ScoreCalculator implements CalculateScore {

    private RobotFactory robotFactory;
    private ResourceFactory resourceFactory;
    private SimConfig config;

    public ScoreCalculator(RobotFactory robotFactory, ResourceFactory resourceFactory,
            SimConfig config) {
        this.robotFactory = robotFactory;
        this.resourceFactory = resourceFactory;
        this.config = config;
    }

    @Override
    public double calculateScore(MLMethod method) {
        Simulation currentSimulation = new Simulation(robotFactory, resourceFactory, config);
        SimulationGUI video = new SimulationGUI(currentSimulation);

        return currentSimulation.getFitness();
    }

    @Override
    public boolean shouldMinimize() {
        return false;
    }

    @Override
    public boolean requireSingleThreaded() {
        return false;
    }


}
