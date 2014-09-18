package za.redbridge.simulator.experiment;

import org.apache.commons.math3.stat.StatUtils;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.CalculateScore;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.NNScoreCalculator;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ThresholdedObjectProximityAgentSensor;
import za.redbridge.simulator.sensor.ThresholdedProximityAgentSensor;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by racter on 2014/09/11.
 */
//evaluates one sensor sensitivity complement, trains and gets the best performing NEAT network for this complement
public class TrainController implements Runnable{

    private ExperimentConfig experimentConfig;
    private SimConfig simConfig;
    private MorphologyConfig morphologyConfig;

    //stores fittest network of each epoch
    private final TreeMap<ComparableNEATNetwork,Integer> leaderBoard;

    //stores scores for each neural network during epochs
    private final ConcurrentSkipListSet<ComparableNEATNetwork> scoreCache;

    private final ConcurrentSkipListMap<ComparableMorphology,TreeMap<ComparableNEATNetwork,Integer>> morphologyLeaderboard;

    //the best-performing network for this complement
    private NEATNetwork bestNetwork;

    public TrainController(ExperimentConfig experimentConfig, SimConfig simConfig,
                           MorphologyConfig morphologyConfig,
                           ConcurrentSkipListMap<ComparableMorphology,TreeMap<ComparableNEATNetwork,Integer>> morphologyLeaderboard) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;
        leaderBoard = new TreeMap<>();
        scoreCache = new ConcurrentSkipListSet<>();
        this.morphologyLeaderboard = morphologyLeaderboard;
    }

    public void run() {

        //TODO: make this get population size form Experiment configs instead
        NEATPopulation pop = new NEATPopulation(morphologyConfig.getTotalReadingSize(),2,
                experimentConfig.getPopulationSize());

        pop.setNEATActivationFunction(new ActivationTANH());
        pop.reset();

        CalculateScore scoreCalculator = new NNScoreCalculator(simConfig, experimentConfig,
                morphologyConfig, scoreCache);

        final EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(pop, scoreCalculator);

        int epochs = 1;

        do {
            System.out.println("Controller Trainer Epoch #" + train.getIteration());
            train.iteration();
            epochs++;

            System.out.println("Average performance of controllers in this epoch scored: " + getEpochMeanScore());
            System.out.println("Best-performing controller of this epoch scored " + scoreCache.last().getScore());

            //get the highest-performing network in this epoch, store it in leaderBoard
            leaderBoard.put(scoreCache.last(), train.getIteration());
            scoreCache.clear();

        } while(epochs <= experimentConfig.getMaxEpochs());

        morphologyLeaderboard.put(new ComparableMorphology(morphologyConfig, leaderBoard.lastKey().getScore()), leaderBoard);
    }

    public NEATNetwork getBestNetwork() { return leaderBoard.lastEntry().getKey().getNetwork(); }

    public Map.Entry<ComparableNEATNetwork,Integer> getHighestEntry() { return leaderBoard.lastEntry(); }

    private synchronized double getEpochMeanScore() {

        double[] scores = new double[scoreCache.size()];
        int counter = 0;

        for (ComparableNEATNetwork network: scoreCache) {

            scores[counter] = network.getScore();
            counter++;
        }

        return StatUtils.mean(scores);
    }


}
