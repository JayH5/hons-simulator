package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;

import java.io.*;
import java.text.ParseException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

//example entry point into simulator
/**
 * Created by shsu on 2014/08/19.
 */
public class Experiment {

    //config files for this experiment o

    @Option (name="--experiment-config", usage="Filename for experiment configuration", metaVar="<experiment config>")
    private String experimentConfig;

    @Option (name="--simulation-config", usage="Filename for simulation configuration", metaVar="<simulation config>")
    private String simulationConfig;

    @Option (name="--show-visuals", aliases="-v", usage="Show visualisation for simulation")
    private boolean showVisuals = false;

    public static void main (String[] args) {

        Experiment options = new Experiment();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException c) {
            System.out.println("Error parsing command-line arguments.");
            c.printStackTrace();
            System.exit(1);
        }

        //if we need to show a visualisation
        if (options.showVisuals()) {

            //UGUGGHGHUHGHGGH
            NEATNetwork bestNetwork = readNetwork("bestNetwork.tmp");

        }
        else {

            ExperimentConfig experimentConfiguration = new ExperimentConfig(options.getExperimentConfig());
            SimConfig simulationConfiguration = new SimConfig(options.getSimulationConfig());


            //TODO: work with multiple morphology configs (specifically, filter sensitivities)
            MorphologyConfig morphologyConfig = null;

            try {
                morphologyConfig = new MorphologyConfig(experimentConfiguration.getMorphologyConfigFile());
            } catch (ParseException p) {
                System.out.println("Error parsing morphology file.");
                p.printStackTrace();
            }

            final ConcurrentSkipListMap<MorphologyConfig,TreeMap<ComparableNEATNetwork,Integer> > morphologyScores;

               /*

            ComplementFactory complementFactory = new ComplementFactory(morphologyConfig, 0.3f);
            final List<MorphologyConfig> sensitivityComplements = complementFactory.generateComplementsForTemplate();

            Thread[] complementThreads = new Thread[sensitivityComplements.size()];

            for (int i = 0; i < complementThreads.length; i++) {

                complementThreads[i] = new Thread(new TrainComplement(experimentConfiguration,
                        simulationConfiguration, sensitivityComplements.get(i)));

                complementThreads[i].run();
            }

            for (int i = 0; i < complementThreads.length; i++) {
                try {
                    complementThreads[i].join();
                }
                catch (InterruptedException iex) {

                    System.out.println("Thread interrupted.");
                    iex.printStackTrace();
                }
            }*/


            /*
            TrainController train = new TrainController(experimentConfiguration, simulationConfiguration, morphologyConfig);

            train.run();*/

        }

    }

    private String getExperimentConfig() { return experimentConfig; }
    private String getSimulationConfig() { return simulationConfig; }
    private boolean showVisuals() { return showVisuals; }

    public static void writeNetwork(NEATNetwork network, String filename) {

        try {
            FileOutputStream fileWriter = new FileOutputStream(filename);
            ObjectOutputStream objectWriter = new ObjectOutputStream(fileWriter);

            objectWriter.writeObject(network);
        }
        catch (FileNotFoundException f) {
            System.out.println("File not found, aborting.");
        }
        catch (IOException e) {
            System.out.println("Error writing object to file.");
            e.printStackTrace();
        }
    }

    public static NEATNetwork readNetwork(String filename) {

        Object o = null;

        try {
            FileInputStream fileReader = new FileInputStream(filename);
            ObjectInputStream objectReader = new ObjectInputStream(fileReader);
            o = objectReader.readObject();
        }
        catch (FileNotFoundException f) {
            System.out.println("File not found, aborting.");
            System.exit(0);
        }
        catch (IOException e) {
            System.out.println("Error writing object to file.");
            e.printStackTrace();
            System.exit(0);
        }
        catch (ClassNotFoundException c) {
            System.out.println("Class not found.");
        }

        return (NEATNetwork) o;
    }


}
