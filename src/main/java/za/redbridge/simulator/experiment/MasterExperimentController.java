package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.hetero.CCHIndividual;
import za.redbridge.simulator.factories.ComplementFactory;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by shsu on 2014/09/19.
 */
public class MasterExperimentController {

    private final MorphologyConfig templateMorphology;
    private final ExperimentConfig experimentConfig;
    private final SimConfig simulationConfig;

    private final ConcurrentSkipListMap<ComparableMorphology, CCHIndividual> morphologyScores = new ConcurrentSkipListMap<>();


    public MasterExperimentController(ExperimentConfig experimentConfig, SimConfig simulationConfig, MorphologyConfig templateMorphology
    ) {

        this.experimentConfig = experimentConfig;
        this.simulationConfig = simulationConfig;
        this.templateMorphology = templateMorphology;
    }

    public void testComplements(String outputDir, Set<MorphologyConfig> sensitivityComplements) {

        Thread[] complementThreads = new Thread[sensitivityComplements.size()];
        int i = 0;
        for (MorphologyConfig complement : sensitivityComplements) {

            complementThreads[i] = new Thread(new HeterogeneousTrainController(experimentConfig,
                    simulationConfig, complement, morphologyScores));

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

    Map.Entry<ComparableMorphology, CCHIndividual> topCombo = morphologyScores.lastEntry();
    NEATNetwork bestNetwork = topCombo.getValue().getNetwork();
    MorphologyConfig bestMorphology = topCombo.getKey().getMorphology();

    IOUtils.writeNetwork(bestNetwork,outputDir,"bestNetwork.tmp");
    bestMorphology.dumpMorphology(outputDir,"bestMorphology.yml");

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

            final HeterogeneousTrainController trainer = new HeterogeneousTrainController(experimentConfig, simulationConfig,
                    entry.getKey(), morphologyScores, testSetID, testSetSerial);

            trainer.run();
        }
    }

}
