package za.redbridge.simulator.experiment;

import org.encog.ml.CalculateScore;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.ScoreCalculator;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

import java.text.ParseException;

//example entry point into simulator
/**
 * Created by shsu on 2014/08/19.
 */
public class Experiment {

    //config files for this experiment

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
        }
        else {

            ExperimentConfig experimentConfiguration = new ExperimentConfig(options.getExperimentConfig());
            SimConfig simulationConfiguration = new SimConfig(options.getSimulationConfig());

            //TODO: work with multiple morphology configs (specifically, filter sensitivities)
            MorphologyConfig morphologyConfig = null;

            try {
                morphologyConfig = new MorphologyConfig(experimentConfiguration.getMorphologyConfigFile());
            }
            catch(ParseException p) {
                System.out.println("Error parsing morphology file.");
                p.printStackTrace();
            }

            //TODO: make this get population size form Experiment configs instead
            NEATPopulation pop = new NEATPopulation(morphologyConfig.getNumSensors(),2,1000);
            pop.reset();

            CalculateScore scoreCalculator = new ScoreCalculator(simulationConfiguration, morphologyConfig);

            final EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(pop, scoreCalculator);

            int epochs = 0;

            do {
                train.iteration();
                System.out.println("Epoch #" + train.getIteration() + pop.getSpecies().size());
                epochs++;
            } while(epochs <= experimentConfiguration.getMaxEpochs());

            NEATNetwork bestNetwork = (NEATNetwork)train.getCODEC().decode(train.getBestGenome());

        }


    }

    private String getExperimentConfig() { return experimentConfig; }
    private String getSimulationConfig() { return simulationConfig; }
    private boolean showVisuals() { return showVisuals; }


}
