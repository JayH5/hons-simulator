package za.redbridge.simulator.ea;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;

import org.encog.neural.neat.NEATNetwork;
import org.jbox2d.dynamics.World;
import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.factories.ResourceFactory;
import za.redbridge.simulator.factories.RobotFactory;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.NEATPhenotype;

import java.awt.*;

/**
 * Created by shsu on 2014/08/13.
 */

//
public class ScoreCalculator implements CalculateScore {

    private SimConfig config;
    private MorphologyConfig morphologyConfig;

    public ScoreCalculator(SimConfig config, MorphologyConfig morphologyConfig) {
        this.config = config;
        this.morphologyConfig = morphologyConfig;
    }

    //MLMethod should be NEATNetwork which we calculate the score for
    @Override
    public double calculateScore(MLMethod method) {

        //TODO: generalise the phenotype instead of hard-coding it
        HomogeneousRobotFactory robotFactory = new HomogeneousRobotFactory(
                new NEATPhenotype(morphologyConfig.getSensorList(), (NEATNetwork) method), 0.7, 0.15,
                new Color(0,0,0));

        Simulation currentSimulation = new Simulation(config, robotFactory);

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
