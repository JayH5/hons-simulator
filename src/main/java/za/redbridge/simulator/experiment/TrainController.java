package za.redbridge.simulator.experiment;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.encog.ml.CalculateScore;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.hetero.CCHIndividual;
import za.redbridge.simulator.ea.hetero.NEATTeam;
import za.redbridge.simulator.ea.neat.CCHCalculateScore;
import za.redbridge.simulator.ea.neat.CCHNEATPopulation;
import za.redbridge.simulator.ea.neat.CCHNEATTrainer;
import za.redbridge.simulator.ea.neat.CNNEATUtil;
import za.redbridge.simulator.factories.HeteroTeamRobotFactory;
import za.redbridge.simulator.factories.TeamPhenotypeFactory;

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

    //stores fittest individual of each epoch
    private final TreeMap<CCHIndividual,Integer> leaderBoard;

    //stores best performing morphology and controller combinations
    private final ConcurrentSkipListMap<ComparableMorphology,CCHIndividual> morphologyLeaderboard;

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
                           ConcurrentSkipListMap<ComparableMorphology,CCHIndividual> morphologyLeaderboard,
                           boolean threadSubruns, long testSetID, long testSetSerial) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;
        leaderBoard = new TreeMap<>();
        this.morphologyLeaderboard = morphologyLeaderboard;
        this.threadSubruns = threadSubruns;

        this.thisIP = ExperimentUtils.getIP();
        this.testSetID = testSetID;

        this.previousCache = new double[experimentConfig.getPopulationSize()];
    }

    public TrainController(ExperimentConfig experimentConfig, SimConfig simConfig,
                           MorphologyConfig morphologyConfig,
                           ConcurrentSkipListMap<ComparableMorphology,CCHIndividual> morphologyLeaderboard,
                           boolean threadSubruns) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;
        leaderBoard = new TreeMap<>();
        this.morphologyLeaderboard = morphologyLeaderboard;
        this.threadSubruns = threadSubruns;

        this.thisIP = ExperimentUtils.getIP();

        this.previousCache = new double[experimentConfig.getPopulationSize()];
    }

    public void run() {

        final CCHNEATPopulation pop = new CCHNEATPopulation(morphologyConfig.getTotalReadingSize(),2,
                experimentConfig.getPopulationSize());

        pop.reset();

        CalculateScore scoreCalculator = new CCHCalculateScore();

        final CCHNEATTrainer train = CNNEATUtil.constructCCHNEATTrainer(pop, scoreCalculator, experimentConfig, simConfig,
                morphologyConfig);

        CCHIndividual lastBestIndividual = new CCHIndividual();

        controllerTrainingLogger.info("Testset ID: " + testSetID);
        controllerTrainingLogger.info("Sensitivity values: \n" + morphologyConfig.sensitivitiesToString());
        controllerTrainingLogger.info("Epoch# \t Mean \t Best \t Cooperative Score of Best \t Variance \t MannWhitneyU");
        do {

            System.out.println("Epoch " + train.getIteration() + ".");

            int epochs = train.getIteration()+1;
            Instant start = Instant.now();

            train.iteration();

            controllerTrainingLogger.info(epochs + "\t" + train.getEpochMean() + "\t" + train.getBestIndividual().getAverageTaskScore() + "\t" + train.getBestIndividual().getAverageCooperativeScore() +
                    "\t" + train.getVariance() + "\t" + train.mannWhitneyImprovementTest());
            /*
            if (epochs % 50 == 0 && train.getBestIndividual().compareTo(lastBestIndividual) > 0) {
                IOUtils.writeNetwork(train.getBestIndividual().getNetwork(), "results/" + ExperimentUtils.getIP() + "/", morphologyConfig.getSensitivityID() + "best_network_at_" + epochs + ".tmp");
                morphologyConfig.dumpMorphology("results/" + ExperimentUtils.getIP() + "/", morphologyConfig.getSensitivityID() + "best_morphology_at_" + epochs + ".tmp");
                lastBestIndividual = train.getBestIndividual();
            }*/

            long minutes = Duration.between(start, Instant.now()).toMinutes();
            controllerTrainingLogger.debug("Epoch took " + minutes + " minutes.");

        } while(train.getIteration()+1 <= experimentConfig.getMaxEpochs());
        train.finishTraining();

        morphologyLeaderboard.put(new ComparableMorphology(morphologyConfig, train.getBestIndividual().getAverageTaskScore()),
                train.getBestIndividual());

        IOUtils.writeNetwork(morphologyLeaderboard.lastEntry().getValue().getNetwork(), "results/" + ExperimentUtils.getIP() + "/", morphologyConfig.getSensitivityID() + "bestNetwork" + testSetID + ".tmp");
        morphologyConfig.dumpMorphology("results/" + ExperimentUtils.getIP(), morphologyConfig.getSensitivityID() + "bestMorphology" + testSetID + ".tmp");

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

        NEATTeam teamWithBestGenotype = lastBestIndividual.getTeam();

        TeamPhenotypeFactory phenotypeFactory = new TeamPhenotypeFactory(morphologyConfig, teamWithBestGenotype.getGenotypes());

        HeteroTeamRobotFactory heteroFactory = new HeteroTeamRobotFactory(phenotypeFactory.generatePhenotypeTeam(),
                simConfig.getRobotMass(), simConfig.getRobotRadius(), simConfig.getRobotColour());

        Simulation simulation = new Simulation(simConfig, heteroFactory, teamWithBestGenotype.getGenotypes());
        simulation.run();

        SimulationGUI video = new SimulationGUI(simulation);

        //new console which displays this simulation
        sim.display.Console console = new sim.display.Console(video);
        console.setVisible(true);
    }


}
