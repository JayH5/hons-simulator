package za.redbridge.simulator.ea;

import org.apache.commons.math3.stat.StatUtils;
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
import za.redbridge.simulator.experiment.ComparableNEATNetwork;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.factories.ResourceFactory;
import za.redbridge.simulator.factories.RobotFactory;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.ChasingPhenotype;
import za.redbridge.simulator.phenotype.NEATPhenotype;

import java.awt.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by shsu on 2014/08/13.
 */

//
public class NNScoreCalculator implements CalculateScore {

    private SimConfig config;
    private MorphologyConfig morphologyConfig;
    private ExperimentConfig experimentConfig;

    //stores fitnesses of population
    private final ConcurrentSkipListSet<ComparableNEATNetwork> scoreCache;

    public NNScoreCalculator(SimConfig config, ExperimentConfig experimentConfig,
                             MorphologyConfig morphologyConfig, ConcurrentSkipListSet<ComparableNEATNetwork> scoreCache) {
        this.config = config;
        this.morphologyConfig = morphologyConfig;
        this.experimentConfig = experimentConfig;
        this.scoreCache = scoreCache;
    }

    //MLMethod should be NEATNetwork which we calculate the score for
    @Override
    public double calculateScore(MLMethod method) {

        //average the performance of this genotype over a few runs of the simulation (standardise on seeds?)
        int testRuns = experimentConfig.getRunsPerGenome();
        double[] performances = new double[testRuns];
        Thread[] simThreads = new Thread[testRuns];

        //TODO: generalise the phenotype instead of hard-coding it
        HomogeneousRobotFactory robotFactory = new HomogeneousRobotFactory(
                new NEATPhenotype(morphologyConfig.getSensorList(), (NEATNetwork) method,
                        morphologyConfig.getTotalReadingSize()), config.getRobotMass(),
                config.getRobotRadius(), config.getRobotColour(), config.getObjectsRobots());

        for (int i = 0; i < testRuns; i++) {

            Simulation simulation = new Simulation(config, robotFactory, config.getObjectsRobots());
            SimRun simulationRunner = new SimRun(simulation, performances, i);

            simThreads[i] = new Thread(simulationRunner);
            simThreads[i].run();
        }

        for (int i = 0; i < testRuns; i++) {

            try {
                simThreads[i].join();
            }
            catch (InterruptedException iex) {

                System.out.println("Thread interrupted.");
                iex.printStackTrace();
            }
        }

        double score = StatUtils.mean(performances);

        scoreCache.add(new ComparableNEATNetwork((NEATNetwork) method, score));

        System.out.println("Score for this genome: " + score);

        return score;
    }


    @Override
    public boolean shouldMinimize() {
        return false;
    }

    @Override
    public boolean requireSingleThreaded() {
        return false;
    }

    private class SimRun implements Runnable {

        private final Simulation simulation;
        private final double[] scores;
        private int ticketNo;


        public SimRun(Simulation simulation, double[] scores, int ticketNo) {

            this.simulation = simulation;
            this.scores = scores;
            this.ticketNo = ticketNo;
        }

        public void run() {

            simulation.run();
            scores[ticketNo] = simulation.getFitness();
        }
    }


}
