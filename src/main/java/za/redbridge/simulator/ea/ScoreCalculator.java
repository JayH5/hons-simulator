package za.redbridge.simulator.ea;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.HomogeneousRobotFactory;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.phenotype.SimplePhenotype;

import java.awt.*;

/**
 * Created by shsu on 2014/08/13.
 */
public class ScoreCalculator implements CalculateScore {

    private RobotFactory factory;
    private SimConfig config;

    public ScoreCalculator(RobotFactory factory, SimConfig config) {

        this.factory = factory;
        this.config = config;

    }

    @Override
    public double calculateScore(MLMethod method) {

        SimConfig config = new SimConfig();
        HomogeneousRobotFactory rf = new HomogeneousRobotFactory(new SimplePhenotype(), 20.0, 2.0, new Color(106,128,200), config.getEnvSize(), config.getSeed());

        Simulation currentSimulation = new Simulation(factory, config);
        SimulationGUI video = new SimulationGUI(currentSimulation);

        double fitness = currentSimulation.getFitness();

        return fitness;
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
