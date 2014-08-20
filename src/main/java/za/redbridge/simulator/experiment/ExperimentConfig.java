package za.redbridge.simulator.experiment;

/**
 * Created by shsu on 2014/08/19.
 */

//parameters for experiment configuration
public class ExperimentConfig {

    public enum EvolutionaryAlgorithm {
        NEAT, EVOLUTIONARY_STRATEGY, GENETIC_PROGRAMMING;
    }

    protected long maxEpochs;
    protected EvolutionaryAlgorithm algorithm;

    public ExperimentConfig() {
        this.maxEpochs = 100;
        this.algorithm = EvolutionaryAlgorithm.NEAT;
    }

    public ExperimentConfig(String filename) { throw new RuntimeException("TODO: Read configs from file."); }

    public ExperimentConfig(EvolutionaryAlgorithm algorithm, long maxEpochs) {
        this.algorithm = algorithm;
        this.maxEpochs = maxEpochs;
    }

    public long getMaxEpochs() { return maxEpochs; }
    public EvolutionaryAlgorithm getEvolutionaryAlgorithm() { return algorithm; }



}
