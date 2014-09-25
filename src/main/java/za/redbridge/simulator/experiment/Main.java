package za.redbridge.simulator.experiment;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.ComplementFactory;

import java.util.Set;


//example entry point into simulator
/**
 * Created by shsu on 2014/08/19.
 */
public class Main {

    //config files for this experiment o

    @Option(name = "--experiment-config", usage = "Filename for experiment configuration", metaVar = "<experiment config>")
    private String experimentConfig;

    @Option(name = "--simulation-config", usage = "Filename for simulation configuration", metaVar = "<simulation config>")
    private String simulationConfig;

    @Option(name = "--show-visuals", aliases = "-v", usage = "Show visualisation for simulation")
    private boolean showVisuals = false;

    @Option(name = "--morphology", aliases = "-m", usage = "The morphology file name for visualisation")
    private String morphologyDump;

    @Option(name = "--controller", aliases = "-nn", usage = "The neural network file name for visualisation")
    private String nnDump;

    @Option(name = "--multihost", aliases = "-p", usage = "Generate complements and assign them to be processed by the given list of hosts")
    private String hosts;

    @Option(name = "--testComplementSet", aliases = "-t", usage = "Search for this complement set in the shared directories and test them")
    private String timestamp;

    @Option(name = "--evolve-complements", aliases = "-e", usage = "Evolve sensor sensitivity complements using a Genetic Algorithm")
    private boolean evolveComplements = false;

    public static void main(String[] args) {

        Main options = new Main();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException c) {
            System.out.println("Error parsing command-line arguments.");
            c.printStackTrace();
            System.exit(1);
        }

        ExperimentConfig experimentConfiguration = new ExperimentConfig(options.getExperimentConfig());
        SimConfig simulationConfiguration = new SimConfig(options.getSimulationConfig());
        MorphologyConfig morphologyConfig = new MorphologyConfig(experimentConfiguration.getMorphologyConfigFile());

        MasterExperimentController masterExperimentController = new MasterExperimentController(experimentConfiguration, simulationConfiguration,
                morphologyConfig, options.evolveComplements, true, true);

        if (options.timestamp != null) {

            masterExperimentController.testAssignedMorphologies(Long.parseLong(options.timestamp));
            System.exit(0);
        }

        if (options.hosts == null) {
            //TODO: work with multiple morphology configs (specifically, filter sensitivities)
            //if we need to show a visualisation
            if (options.showVisuals()) {

                SimulationVisual simulationVisual = new SimulationVisual(simulationConfiguration, options.nnDump, options.morphologyDump);
                simulationVisual.run();

            } else {
                masterExperimentController.start();
            }
        }

        //if a host file is provided, generate some complements and split them over the given hosts
        else {

            ComplementFactory complementFactory = new ComplementFactory(morphologyConfig,
                    experimentConfiguration.getComplementGeneratorResolution());

            final Set<MorphologyConfig> sensitivityComplements = complementFactory.generateComplementsForTemplate();
            ComplementDistributor complementDistributor = new ComplementDistributor(options.hosts, sensitivityComplements);

            complementDistributor.assignHosts();
            complementDistributor.writeMorphologiesToAssignment();
            System.out.println("Assigned morphologies to list of IPs.");
            System.exit(0);
        }
    }

    private String getExperimentConfig() {
        return experimentConfig;
    }

    private String getSimulationConfig() {
        return simulationConfig;
    }

    private boolean showVisuals() {
        return showVisuals;
    }

}
