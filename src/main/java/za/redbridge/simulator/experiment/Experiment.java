package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import sim.display.*;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.ComplementFactory;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.phenotype.ChasingPhenotype;
import za.redbridge.simulator.phenotype.NEATPhenotype;

import java.io.*;
import java.io.Console;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

//example entry point into simulator
/**
 * Created by shsu on 2014/08/19.
 */
public class Experiment {

    //config files for this experiment o

    @Option(name = "--experiment-config", usage = "Filename for experiment configuration", metaVar = "<experiment config>")
    private String experimentConfig;

    @Option(name = "--simulation-config", usage = "Filename for simulation configuration", metaVar = "<simulation config>")
    private String simulationConfig;

    @Option(name = "--show-visuals", aliases = "-v", usage = "Show visualisation for simulation")
    private boolean showVisuals = false;

    @Option(name = "--evolve-complements", aliases = "-e", usage = "Evolve sensor sensitivity complements using a Genetic Algorithm")
    private boolean evolveComplements = false;

    public static void main(String[] args) {

        Experiment options = new Experiment();
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

        //TODO: work with multiple morphology configs (specifically, filter sensitivities)
        MorphologyConfig morphologyConfig = null;

        //if we need to show a visualisation
        if (options.showVisuals()) {

            NEATNetwork bestNetwork = IOUtils.readNetwork("bestNetwork.tmp");

            try {
                morphologyConfig = new MorphologyConfig("bestMorphology.yml");
            } catch (ParseException p) {
                System.out.println("Error parsing morphology file.");
                p.printStackTrace();
            }

            HomogeneousRobotFactory robotFactory = new HomogeneousRobotFactory(
                    new NEATPhenotype(morphologyConfig.getSensorList(), bestNetwork, morphologyConfig.getTotalReadingSize()),
                    simulationConfiguration.getRobotMass(),
                    simulationConfiguration.getRobotRadius(), simulationConfiguration.getRobotColour(),
                    simulationConfiguration.getObjectsRobots());

            Simulation simulation = new Simulation(simulationConfiguration, robotFactory);

            SimulationGUI video =
                    new SimulationGUI(simulation);

            //new console which displays this simulation
            sim.display.Console console = new sim.display.Console(video);
            console.setVisible(true);

        } else {

            try {
                morphologyConfig = new MorphologyConfig(experimentConfiguration.getMorphologyConfigFile());
            } catch (ParseException p) {
                System.out.println("Error parsing morphology file.");
                p.printStackTrace();
            }

            MasterExperimentController masterExperimentController = new MasterExperimentController(experimentConfiguration, simulationConfiguration,
                    morphologyConfig, options.evolveComplements, true, true);

            masterExperimentController.start();

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

    private boolean evolveComplements() {
        return evolveComplements;
    }


}
