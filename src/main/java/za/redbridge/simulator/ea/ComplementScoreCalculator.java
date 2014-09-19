package za.redbridge.simulator.ea;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.experiment.ComparableMorphology;
import za.redbridge.simulator.experiment.ComparableNEATNetwork;
import za.redbridge.simulator.experiment.TrainComplement;
import za.redbridge.simulator.experiment.TrainController;
import za.redbridge.simulator.factories.ComplementFactory;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by shsu on 2014/09/16.
 */
public class ComplementScoreCalculator implements CalculateScore {


    private ExperimentConfig experimentConfig;
    private SimConfig simConfig;
    private MorphologyConfig morphologyConfig;

    //stores fittest morphology of each epoch
    private final TreeMap<ComparableMorphology,Integer> leaderBoard;

    //stores scores for each morphology during epochs
    private final ConcurrentSkipListSet<ComparableMorphology> scoreCache;

    private final ConcurrentSkipListMap<ComparableMorphology,TreeMap<ComparableNEATNetwork,Integer>> morphologyScores;

    public ComplementScoreCalculator(SimConfig simConfig, ExperimentConfig experimentConfig,
                                     MorphologyConfig morphologyConfig,
                                     ConcurrentSkipListSet<ComparableMorphology> scoreCache, ConcurrentSkipListMap<ComparableMorphology,TreeMap<ComparableNEATNetwork,Integer>> morphologyScores) {
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;
        this.experimentConfig = experimentConfig;
        this.scoreCache = scoreCache;
        this.morphologyScores = morphologyScores;

        leaderBoard = new TreeMap<>();
    }

    @Override
    public double calculateScore(MLMethod method) {

        SensitivityGenome sensitivityGenome = (SensitivityGenome) method;
        MorphologyConfig morphology =
                MorphologyConfig.MorphologyFromSensitivities(morphologyConfig, sensitivityGenome.getData());

        TrainComplement complementTrainer = new TrainComplement(experimentConfig, simConfig, morphology, morphologyScores);
        complementTrainer.run();

        double score = complementTrainer.getHighestEntry().getValue();
        System.out.println("Top score for this sensor sensitivity complement is " + score);

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
}
