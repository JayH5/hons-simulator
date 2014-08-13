package za.redbridge.simulator.ea;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.interfaces.RobotFactory;

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

        Simulation simulation = new Simulation(factory, config);



        return 0;
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
