package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.ComplementFactory;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

    public void start() {

        //Evolve complements instead of generating them
        if (evolveComplements) {

            TrainComplement complementGA = new TrainComplement(experimentConfig, simulationConfig,
                    templateMorphology, morphologyScores);

            complementGA.run();
        }
        else {

            ComplementFactory complementFactory = new ComplementFactory(templateMorphology,
                    experimentConfig.getComplementGeneratorResolution());

            final Set<MorphologyConfig> sensitivityComplements = complementFactory.generateComplementsForTemplate();

            if (threadComplementTraining) {
                Thread[] complementThreads = new Thread[sensitivityComplements.size()];

                int i = 0;
                for (MorphologyConfig complement : sensitivityComplements) {

                    ComplementFactory.printArray(complement.getSensitivities());
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

        }

        Map.Entry<ComparableMorphology, TreeMap<ComparableNEATNetwork, Integer>> topCombo = morphologyScores.lastEntry();
        NEATNetwork bestNetwork = topCombo.getValue().lastKey().getNetwork();
        MorphologyConfig bestMorphology = topCombo.getKey().getMorphology();

        IOUtils.writeNetwork(bestNetwork, "bestNetwork.tmp");
        bestMorphology.dumpMorphology("bestMorphology.yml");
    }
}
