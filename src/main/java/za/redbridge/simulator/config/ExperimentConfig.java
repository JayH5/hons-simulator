package za.redbridge.simulator.config;

import org.yaml.snakeyaml.Yaml;
import za.redbridge.simulator.ea.DefaultFitnessFunction;
import za.redbridge.simulator.ea.FitnessFunction;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.factories.RobotFactory;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by shsu on 2014/08/19.
 */

//parameters for experiment configuration
public class ExperimentConfig extends Config {

    private static final FitnessFunction DEFAULT_FITNESS_FUNCTION = new DefaultFitnessFunction();
    private static final long DEFAULT_MAX_EPOCHS = 1000;
    private static final String DEFAULT_FACTORY = "za.redbridge.simulator.factories.HomogeneousRobotFactory";
    private static final String DEFAULT_MORPHOLOGY_FILEPATH= "sensorlist.yml";

    public enum EvolutionaryAlgorithm {
        NEAT, EVOLUTIONARY_STRATEGY, GENETIC_PROGRAMMING;
    }

    protected long maxEpochs;
    protected EvolutionaryAlgorithm algorithm;
    protected RobotFactory factory;
    protected FitnessFunction fitnessFunction;

    public ExperimentConfig() {
        this.maxEpochs = 100;
        this.algorithm = EvolutionaryAlgorithm.NEAT;
    }

    public ExperimentConfig(String filepath) {

        Yaml yaml = new Yaml();

        Map<String, Object> config = null;

        try (Reader reader = Files.newBufferedReader(Paths.get(filepath))) {
            config = (Map<String, Object>) yaml.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //default values
        FitnessFunction fitness = DEFAULT_FITNESS_FUNCTION;

        // Fitness function
        Map fitnessFunc = (Map) config.get("scoring");
        if (checkFieldPresent(fitnessFunc, "scoring")) {
            String fitnessF = (String) fitnessFunc.get("fitnessFunction");
            if (checkFieldPresent(fitnessF, "scoring:fitnessFunction")) {

                try {
                    Class f = Class.forName(fitnessF);
                    Object o = f.newInstance();

                    if (!(o instanceof FitnessFunction)) {
                        throw new InvalidClassException("");
                    }

                    fitness = (FitnessFunction) o;
                }
                catch (ClassNotFoundException c) {
                    System.out.println("Invalid class name specified in SimConfig: " + fitnessF + ". Using default fitness function.");
                    c.printStackTrace();
                }
                catch (InvalidClassException i) {
                    System.out.println("Invalid specified fitness class. " + fitnessF + ". Using default fitness function.");
                    i.printStackTrace();
                }
                catch (InstantiationException ins) {
                    ins.printStackTrace();
                }
                catch (IllegalAccessException ill) {
                    ill.printStackTrace();
                }
            }
        }

        throw new RuntimeException("TODO: Read configs from file.");

    }

    public ExperimentConfig(EvolutionaryAlgorithm algorithm, long maxEpochs) {
        this.algorithm = algorithm;
        this.maxEpochs = maxEpochs;
    }

    public long getMaxEpochs() { return maxEpochs; }
    public EvolutionaryAlgorithm getEvolutionaryAlgorithm() { return algorithm; }



}
