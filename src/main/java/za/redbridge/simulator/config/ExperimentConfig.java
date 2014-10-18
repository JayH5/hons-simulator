package za.redbridge.simulator.config;

import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by shsu on 2014/08/19.
 */

//parameters for experiment configuration
public class ExperimentConfig extends Config {

    private static final long DEFAULT_MAX_EPOCHS = 1000;
    private static final EvolutionaryAlgorithm DEFAULT_CONTROLLER_EA = EvolutionaryAlgorithm.NEAT;
    private static final int DEFAULT_POPULATION_SIZE = 15;
    private static final String DEFAULT_MORPHOLOGY_FILEPATH= "sensorList.yml";
    private static final int DEFAULT_RUNS_PER_GENOME = 1;

    public enum EvolutionaryAlgorithm {
        NEAT, EVOLUTIONARY_STRATEGY, GENETIC_PROGRAMMING;
    }

    protected final long maxEpochs;
    protected final int populationSize;
    protected final int runsPerGenome;

    protected EvolutionaryAlgorithm algorithm;
    protected String robotFactory;
    protected String morphologyConfigFile;


    public ExperimentConfig() {
        this.maxEpochs = DEFAULT_MAX_EPOCHS;
        this.populationSize = DEFAULT_POPULATION_SIZE;
        this.algorithm = EvolutionaryAlgorithm.NEAT;
        this.runsPerGenome = DEFAULT_RUNS_PER_GENOME;
    }

    public ExperimentConfig(String filepath) {

        Yaml yaml = new Yaml();
        Map<String, Object> config = null;

        try (Reader reader =
                     Files.newBufferedReader(Paths.get(filepath), Charset.defaultCharset())) {
            config = (Map<String, Object>) yaml.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //default values
        long maxEpochs = DEFAULT_MAX_EPOCHS;
        ExperimentConfig.EvolutionaryAlgorithm controllerEA = DEFAULT_CONTROLLER_EA;
        int popSize = DEFAULT_POPULATION_SIZE;
        String morphologyFile = DEFAULT_MORPHOLOGY_FILEPATH;
        int runsPerG = DEFAULT_RUNS_PER_GENOME;

        Map control = (Map) config.get("control");
        if (checkFieldPresent(control, "control")) {

            Number epochs = (Number) control.get("maxEpochs");
            if (checkFieldPresent(epochs, "control:maxEpochs")) {
                maxEpochs = epochs.longValue();
            }
            Integer runsPG = (Integer) control.get("runsPerGenome");
            if (checkFieldPresent(runsPG, "control:runsPerGenome")) {
                runsPerG = runsPG;
            }
        }

        /*
        Map phenotype = (Map) config.get("phenotype");
        if (checkFieldPresent(phenotype, "phenotype")) {

            String fact = (String) phenotype.get("factory");
            if (checkFieldPresent(fact, "phenotype:factory")) {
                factory = fact;
            }
        }*/

        Map ea = (Map) config.get("evolutionaryAlgorithm");
        if (checkFieldPresent(ea, "evolutionaryAlgorithm")) {

            String EA = (String) ea.get("controllerEA");
            if (checkFieldPresent(EA, "evolutionaryAlgorithm:controllerEA")) {

                if (EA.trim().equals(EvolutionaryAlgorithm.NEAT.name())) {
                    controllerEA = Enum.valueOf(EvolutionaryAlgorithm.class, EA);
                }
                else {
                    System.out.println("Only NEAT is supported in this version: using NEAT algorithm.");
                }
            }

            Integer pSize = (Integer) ea.get("populationSize");
            if (checkFieldPresent(EA, "evolutionaryAlgorithm:populationSize")) {
                popSize = pSize;
            }
        }

        Map morphology = (Map) config.get("morphology");
        if (checkFieldPresent(morphology, "morphology")) {

            String morphFile = (String) morphology.get("morphologyFileName");
            if (checkFieldPresent(morphFile, "morphology:morphologyFileName")) {
                morphologyFile = morphFile;
            }
        }

        this.maxEpochs = maxEpochs;
        this.algorithm = controllerEA;
        this.populationSize = popSize;

        this.morphologyConfigFile = morphologyFile;
        this.runsPerGenome = runsPerG;
    }

    public ExperimentConfig(long maxEpochs, EvolutionaryAlgorithm algorithm, int populationSize, int runsPerGenome,
                            String robotFactory, String morphologyConfigFile) {

        this.maxEpochs = maxEpochs;
        this.algorithm = algorithm;
        this.populationSize = populationSize;
        this.robotFactory = robotFactory;
        this.morphologyConfigFile = morphologyConfigFile;
        this.runsPerGenome = runsPerGenome;
    }

    public long getMaxEpochs() { return maxEpochs; }

    public EvolutionaryAlgorithm getEvolutionaryAlgorithm() { return algorithm; }

    public int getPopulationSize() { return populationSize; }

    public String getRobotFactory() { return robotFactory; }

    public String getMorphologyConfigFile() { return morphologyConfigFile; }

    public int getRunsPerGenome() { return runsPerGenome; }

}
