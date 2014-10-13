package za.redbridge.simulator.experiment;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.encog.ml.CalculateScore;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.NNScoreCalculator;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
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

    private final boolean threadSubruns;

    private final String thisIP;

    private long testSetID;

    private long testSetSerial;

    private double[] previousCache;

    //the best-performing network for this complement
    private NEATNetwork bestNetwork;

    private static Logger controllerTrainingLogger = LoggerFactory.getLogger(TrainController.class);

    public TrainController(ExperimentConfig experimentConfig, SimConfig simConfig,
                           MorphologyConfig morphologyConfig,
                           ConcurrentSkipListMap<ComparableMorphology,TreeMap<ComparableNEATNetwork,Integer>> morphologyLeaderboard,
                           boolean threadSubruns, long testSetID, long testSetSerial) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;
        leaderBoard = new TreeMap<>();
        scoreCache = new ConcurrentSkipListSet<>();
        this.morphologyLeaderboard = morphologyLeaderboard;
        this.threadSubruns = threadSubruns;

        this.thisIP = ExperimentUtils.getIP();
        this.testSetID = testSetID;

        this.previousCache = new double[experimentConfig.getPopulationSize()];
    }

    public TrainController(ExperimentConfig experimentConfig, SimConfig simConfig,
                           MorphologyConfig morphologyConfig,
                           ConcurrentSkipListMap<ComparableMorphology,TreeMap<ComparableNEATNetwork,Integer>> morphologyLeaderboard,
                           boolean threadSubruns) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;
        leaderBoard = new TreeMap<>();
        scoreCache = new ConcurrentSkipListSet<>();
        this.morphologyLeaderboard = morphologyLeaderboard;
        this.threadSubruns = threadSubruns;

        this.thisIP = ExperimentUtils.getIP();

        this.previousCache = new double[experimentConfig.getPopulationSize()];
    }

    public void run() {

        //TODO: make this get population size form Experiment configs instead
        NEATPopulation pop = new NEATPopulation(morphologyConfig.getTotalReadingSize(),2,
                experimentConfig.getPopulationSize());
        pop.setInitialConnectionDensity(0.5);
        pop.reset();

        CalculateScore scoreCalculator = new NNScoreCalculator(simConfig, experimentConfig,
                morphologyConfig, scoreCache, threadSubruns);

        final EvolutionaryAlgorithm train = CNNEATUtil.constructNEATTrainer(pop, scoreCalculator);

        Genome previousBest = train.getBestGenome();
        NEATCODEC neatCodec = new NEATCODEC();

        controllerTrainingLogger.info("Testset ID: " + testSetID);
        controllerTrainingLogger.info("Sensitivity values: \n" + morphologyConfig.parametersToString());
        controllerTrainingLogger.info("Epoch# \t Mean \t Best \t Variance \t MannWhitneyU");
        do {

            int epochs = train.getIteration()+1;
            Instant start = Instant.now();

            train.iteration();

            if (previousBest == null) {
                previousBest = train.getBestGenome();
            }

            controllerTrainingLogger.info(epochs + "\t" + getEpochMeanScore() + "\t" + scoreCache.last().getScore() +
                    "\t" + getVariance() + "\t" + mannWhitneyImprovementTest());


            if (epochs % 50 == 0 && previousBest != train.getBestGenome()) {
                IOUtils.writeNetwork((NEATNetwork) neatCodec.decode(train.getBestGenome()), "results/" + ExperimentUtils.getIP() + "/", morphologyConfig.getMorphologyID() + "best_network_at_" + epochs + ".tmp");
                morphologyConfig.dumpMorphology("results/" + ExperimentUtils.getIP() + "/", morphologyConfig.getMorphologyID() + "best_morphology_at_" + epochs + ".tmp");
                previousBest = train.getBestGenome();
            }

            //get the highest-performing network in this epoch, store it in leaderBoard
            leaderBoard.put(scoreCache.last(), train.getIteration());
            previousCache = getEpochScoreData();
            scoreCache.clear();

            long minutes = Duration.between(start, Instant.now()).toMinutes();
            controllerTrainingLogger.debug("Epoch took " + minutes + " minutes.");

        } while(train.getIteration()+1 <= experimentConfig.getMaxEpochs());
        train.finishTraining();

        morphologyLeaderboard.put(new ComparableMorphology(morphologyConfig, previousBest.getScore()), leaderBoard);

        IOUtils.writeNetwork((NEATNetwork) neatCodec.decode(train.getBestGenome()), "results/" + ExperimentUtils.getIP() + "/", morphologyConfig.getMorphologyID() + "bestNetwork" + testSetID + ".tmp");
        morphologyConfig.dumpMorphology("results/" + ExperimentUtils.getIP(), morphologyConfig.getMorphologyID() + "bestMorphology" + testSetID + ".tmp");

        //delete this morphology file if it was a result of the multihost operation
        Path morphologyPath = Paths.get("shared/" + ExperimentUtils.getIP() + "/"+ testSetID + ":" + testSetSerial + ".morphology");

        try {
            Files.delete(morphologyPath);
        } catch (NoSuchFileException x) {
            System.err.format("Error deleting morphology: %s: no such" + " file or directory%n", morphologyPath);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", morphologyPath);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }
    }

    public NEATNetwork getBestNetwork() { return leaderBoard.lastEntry().getKey().getNetwork(); }

    private synchronized double getEpochMeanScore() { return StatUtils.mean(getEpochScoreData()); }

    private synchronized double[] getEpochScoreData() {

        double[] scores = new double[scoreCache.size()];
        int counter = 0;

        for (ComparableNEATNetwork network: scoreCache) {

            scores[counter] = network.getScore();
            counter++;
        }

        return scores;
    }

    private synchronized double getVariance() { return StatUtils.variance(getEpochScoreData()); }

    private synchronized double mannWhitneyImprovementTest() {

        MannWhitneyUTest mwTest = new MannWhitneyUTest();

        return mwTest.mannWhitneyU(getEpochScoreData(), previousCache);
    }
}
