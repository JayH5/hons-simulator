package za.redbridge.simulator.config;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;

/**
 * Created by shsu on 2014/08/19.
 */

//parameters for experiment configuration
public class ExperimentConfig {

    public enum EA {
        NEAT, EVOLUTIONARY_STRATEGY, GENETIC_PROGRAMMING;
    }

    private static final EA DEFAULT_EA = EA.NEAT;
    private static final long DEFAULT_MAX_EPOCHS = 100;
    private static final boolean DEFAULT_HEAD = false;


    protected long maxEpochs;
    protected EA algorithm;

    public ExperimentConfig() {
        this.maxEpochs = 100;
        this.algorithm = EA.NEAT;
    }

    public ExperimentConfig(String filename) { throw new RuntimeException("TODO: Read configs from file."); }

    public ExperimentConfig(EA algorithm, long maxEpochs) {
        this.algorithm = algorithm;
        this.maxEpochs = maxEpochs;
    }

    public long getMaxEpochs() { return maxEpochs; }
    public EA getEvolutionaryAlgorithm() { return algorithm; }



}
