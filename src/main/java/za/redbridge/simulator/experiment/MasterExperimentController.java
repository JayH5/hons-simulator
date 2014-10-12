package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.ComplementFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by shsu on 2014/09/19.
 */
public class MasterExperimentController {

    private boolean evolveComplements;
    private boolean threadComplementTraining;
    private boolean threadNNSubruns;

    private final MorphologyConfig templateMorphology;
    private final ExperimentConfig experimentConfig;
    private final SimConfig simulationConfig;

    private final ConcurrentSkipListMap<ComparableMorphology, TreeMap<ComparableNEATNetwork, Integer>> morphologyScores = new ConcurrentSkipListMap<>();


    public MasterExperimentController(ExperimentConfig experimentConfig, SimConfig simulationConfig, MorphologyConfig templateMorphology,
                                      boolean evolveComplements, boolean threadComplementTraining, boolean threadNNSubruns) {

        this.experimentConfig = experimentConfig;
        this.simulationConfig = simulationConfig;
        this.templateMorphology = templateMorphology;

        this.evolveComplements = evolveComplements;
        this.threadComplementTraining = threadComplementTraining;
        this.threadNNSubruns = threadNNSubruns;
    }

    public void testComplements(String outputDir, Set<MorphologyConfig> sensitivityComplements) {

        if (threadComplementTraining) {
            Thread[] complementThreads = new Thread[sensitivityComplements.size()];

            int i = 0;
            for (MorphologyConfig complement : sensitivityComplements) {

                complementThreads[i] = new Thread(new TrainController(experimentConfig,
                        simulationConfig, complement, morphologyScores, threadNNSubruns));

                complementThreads[i].run();
                i++;
            }

            for (int j = 0; j < complementThreads.length; j++) {

                try {
                    complementThreads[j].join();
                } catch (InterruptedException iex) {

                    System.out.println("Thread interrupted.");
                    iex.printStackTrace();
                }
            }
        }
        else {

            TrainController[] complementTrainers = new TrainController[sensitivityComplements.size()];

            int i = 0;
            for (MorphologyConfig complement : sensitivityComplements) {

                complementTrainers[i] = new TrainController(experimentConfig,
                        simulationConfig, complement, morphologyScores, threadNNSubruns);

                complementTrainers[i].run();
                i++;
            }

    }

    Map.Entry<ComparableMorphology, TreeMap<ComparableNEATNetwork, Integer>> topCombo = morphologyScores.lastEntry();
    NEATNetwork bestNetwork = topCombo.getValue().lastKey().getNetwork();
    MorphologyConfig bestMorphology = topCombo.getKey().getMorphology();

    IOUtils.writeNetwork(bestNetwork, outputDir, "bestNetwork.tmp");
    bestMorphology.dumpMorphology(outputDir, "bestMorphology.yml");

    }

    public void start() {


            ComplementFactory complementFactory = new ComplementFactory(templateMorphology,
                    experimentConfig.getComplementGeneratorResolution());

            final Set<MorphologyConfig> sensitivityComplements = complementFactory.generateSensitivitiesForTemplate();
            testComplements("results/",sensitivityComplements);
    }

    //test the designated morphologies assigned to this host for this timestamp
    public void testAssignedMorphologies(long timestamp) {

        if (!ExperimentUtils.searchForExperimentSet(timestamp)) {
            System.out.println("Experiment set does not exist; aborting.");
            System.exit(-1);
        }

        String thisIP = ExperimentUtils.getIP();

        //read the morphology set (identified by timestamp) assigned to this IP
        HashMap<MorphologyConfig,String> readMorphologies = ExperimentUtils.readAssignedMorphologies(timestamp, thisIP);

        for (Map.Entry<MorphologyConfig,String> entry: readMorphologies.entrySet()) {

            String[] temp = entry.getValue().split(":");
            long testSetID = Long.parseLong(temp[0]);
            long testSetSerial = Long.parseLong(temp[1].split(".")[0]);

            final TrainController trainer = new TrainController(experimentConfig, simulationConfig,
                    entry.getKey(), morphologyScores, true, testSetID, testSetSerial);

            trainer.run();
        }
    }

}
