package za.redbridge.simulator.ea;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;

import org.encog.neural.neat.NEATNetwork;
import org.jbox2d.dynamics.World;
import sim.display.Console;
import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.factories.ResourceFactory;
import za.redbridge.simulator.factories.RobotFactory;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.ChasingPhenotype;
import za.redbridge.simulator.phenotype.NEATPhenotype;

import java.awt.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by shsu on 2014/08/13.
 */

//
public class ScoreCalculator implements CalculateScore {

    private SimConfig config;
    private MorphologyConfig morphologyConfig;
    private ExperimentConfig experimentConfig;

    //stores strongest 
    private ConcurrentSkipListMap<NEATNetwork, Double> leaderBoard;

    public ScoreCalculator(SimConfig config, ExperimentConfig experimentConfig, MorphologyConfig morphologyConfig) {
        this.config = config;
        this.morphologyConfig = morphologyConfig;
        this.experimentConfig = experimentConfig;
    }

    //MLMethod should be NEATNetwork which we calculate the score for
    @Override
    public double calculateScore(MLMethod method) {

        //TODO: generalise the phenotype instead of hard-coding it
        HomogeneousRobotFactory robotFactory = new HomogeneousRobotFactory(
                new NEATPhenotype(morphologyConfig.getSensorList(), (NEATNetwork) method,
                        morphologyConfig.getTotalReadingSize()), config.getRobotMass(),
                config.getRobotRadius(), config.getRobotColour());

        Simulation simulation = new Simulation(config, experimentConfig, robotFactory);

        /*
        SimulationGUI video =
                new SimulationGUI(simulation);

        //new console which displays this simulation
        Console console = new Console(video);
        console.setVisible(true);*/

        simulation.run();

        return simulation.getFitness();
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
