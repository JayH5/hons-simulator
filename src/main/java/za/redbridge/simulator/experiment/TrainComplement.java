package za.redbridge.simulator.experiment;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.population.BasicPopulation;
import org.encog.ml.ea.population.Population;
import org.encog.ml.ea.species.BasicSpecies;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.ml.genetic.crossover.SpliceNoRepeat;
import org.encog.ml.genetic.mutate.MutateShuffle;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.ComplementScoreCalculator;
import za.redbridge.simulator.ea.SensitivityGenome;
import za.redbridge.simulator.factories.SensitivityGenomeFactory;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by shsu on 2014/09/16.
 */
//gets the best performing complement/controller combination for this morphology
public class TrainComplement implements Runnable {

    private ExperimentConfig experimentConfig;
    private SimConfig simConfig;
    private MorphologyConfig morphologyConfig;

    //stores fittest network of each epoch
    private final TreeMap<ComparableMorphology,Integer> leaderBoard;

    //stores scores for each neural network during epochs
    private final ConcurrentSkipListSet<ComparableMorphology> scoreCache;

    public TrainComplement(ExperimentConfig experimentConfig, SimConfig simConfig,
                           MorphologyConfig morphologyConfig) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;

        leaderBoard = new TreeMap<>();
        scoreCache = new ConcurrentSkipListSet<>();
    }

    public void run() {

        //TODO: make this get population size form Experiment configs instead
        Population pop = initPopulation(experimentConfig.getGAPopulationSize());

        System.out.println("populationsize " + pop.getPopulationSize());

        CalculateScore scoreCalculator = new ComplementScoreCalculator(simConfig, experimentConfig,
                morphologyConfig, scoreCache);

        TrainEA GA = new TrainEA(pop, scoreCalculator);

        GA.addOperation(0.9, new SpliceNoRepeat(morphologyConfig.getNumAdjustableSensitivities() / 3));
        GA.addOperation(0.1, new MutateShuffle());

        int epochs = 0;

        do {
            System.out.println("Complement Trainer Epoch #" + GA.getIteration());
            GA.iteration();
            epochs++;

            //get the highest-performing network in this epoch, store it in leaderBoard
            leaderBoard.put(scoreCache.last(), GA.getIteration());
            scoreCache.clear();

        } while(epochs <= experimentConfig.getMaxEpochs());

    }

    public MorphologyConfig getBestMorphology() { return leaderBoard.lastEntry().getKey().getMorphology(); }

    public Map.Entry<ComparableMorphology,Integer> getHighestEntry() { return leaderBoard.lastEntry(); }

    private Population initPopulation(int populationSize)
    {
        Population result = new BasicPopulation(populationSize, null);
        BasicSpecies defaultSpecies = new BasicSpecies();
        defaultSpecies.setPopulation(result);

        SensitivityGenomeFactory factory = new SensitivityGenomeFactory(morphologyConfig);

        for (int i = 0; i < populationSize; i++) {
            final SensitivityGenome genome = factory.randomGenome();
            defaultSpecies.getMembers().add(genome);
        }

        result.setGenomeFactory(factory);
        result.getSpecies().add(defaultSpecies);

        return result;
    }



}
