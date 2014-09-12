package za.redbridge.simulator.experiment;

import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;

/**
 * Created by racter on 2014/09/11.
 */
//evaluates one sensor sensitivity complement, gets the best performing NEAT network for this complement
public class TrainComplement {

    private ExperimentConfig experimentConfig;
    private SimConfig simConfig;
    private MorphologyConfig morphologyConfig;

    public TrainComplement(ExperimentConfig experimentConfig, SimConfig simConfig,
                           MorphologyConfig morphologyConfig) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;
    }
}
