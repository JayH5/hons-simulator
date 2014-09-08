package za.redbridge.simulator.experiment;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import za.redbridge.simulator.config.ExperimentConfig;

//example entry point into simulator
/**
 * Created by shsu on 2014/08/19.
 */
public class Experiment {


    //config files for this experiment

    @Option (name="--experiment-config", usage="Filename for experiment configuration", metaVar="<experiment config>")
    private String experimentConfig;

    @Option (name="--simulation-config,", usage="Filename for simulation configuration", metaVar="<simulation config>")
    private String simulationConfig;

    @Option (name="--show-visuals", aliases="-v", usage="Show visualisation for simulation")
    private boolean showVisuals = false;

    private ExperimentConfig experimentConfigs;

    public static void main (String[] args) {


        Experiment options = new Experiment();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException c) {
            System.out.println("Error parsing command-line arguments.");
            System.exit(1);
        }

        //if we need to show a visualisation
        if (options.showVisuals()) {

            //UGUGGHGHUHGHGGH
        }
        else {



        }


    }

    private String getExperimentConfig() { return experimentConfig; }
    private String getSimulationConfig() { return simulationConfig; }
    private ExperimentConfig getExperimentConfigs() { return experimentConfigs; }
    private boolean showVisuals() { return showVisuals; }


}
