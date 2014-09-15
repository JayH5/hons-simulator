package za.redbridge.simulator.experiment;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.ScoreCalculator;

import javax.swing.*;
import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by racter on 2014/09/11.
 */
//evaluates one sensor sensitivity complement, gets the best performing NEAT network for this complement
public class TrainComplement {

    private ExperimentConfig experimentConfig;
    private SimConfig simConfig;
    private MorphologyConfig morphologyConfig;

    //stores fittest network of each epoch
    private final TreeMap<ComparableNEATNetwork,Integer> leaderBoard;

    //stores scores for each neural network during epochs
    private final ConcurrentSkipListSet<ComparableNEATNetwork> scoreCache;

    //the best-performing network for this complement
    private NEATNetwork bestNetwork;

    public TrainComplement(ExperimentConfig experimentConfig, SimConfig simConfig,
                           MorphologyConfig morphologyConfig) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;
        leaderBoard = new TreeMap<>();
        scoreCache = new ConcurrentSkipListSet<>();
    }

    public void train() {

        //TODO: make this get population size form Experiment configs instead
        NEATPopulation pop = new NEATPopulation(morphologyConfig.getTotalReadingSize(),2,
                experimentConfig.getPopulationSize());
        pop.reset();

        CalculateScore scoreCalculator = new ScoreCalculator(simConfig, experimentConfig,
                morphologyConfig, scoreCache);

        final EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(pop, scoreCalculator);

        int epochs = 0;

        do {
            System.out.println("Epoch #" + train.getIteration());
            train.iteration();
            epochs++;

            //get the highest-performing network in this epoch, store it in leaderBoard
            leaderBoard.put(scoreCache.last(), train.getIteration());
            scoreCache.clear();

        } while(epochs <= experimentConfig.getMaxEpochs());

    }

    public NEATNetwork getBestNetwork() { return leaderBoard.lastEntry().getKey().getNetwork(); }

    public Map.Entry<ComparableNEATNetwork,Integer> getHighestEntry() { return leaderBoard.lastEntry(); }
}
