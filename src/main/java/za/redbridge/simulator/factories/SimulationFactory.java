package za.redbridge.simulator.factories;

import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.sensor.AgentSensor;

import java.text.ParseException;
import java.util.List;

/**
 * Created by shsu on 2014/09/09.
 */
public class SimulationFactory {

    private final ExperimentConfig experimentConfig;
    private final SimConfig simConfig;

    public SimulationFactory(ExperimentConfig experimentConfig, SimConfig simConfig) {

        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
    }

    //returns a single sensor morphology as read from the morphology file specified in experimentConfig
    private List<AgentSensor> getSensorMorphology() {

        MorphologyConfig morphologyConfig = null;

        try {
            morphologyConfig = new MorphologyConfig(experimentConfig.getMorphologyConfigFile());
        }
        catch(ParseException p) {
            System.out.println("Error parsing morphology file.");
            p.printStackTrace();
        }

        return morphologyConfig.getSensorList();
    }


}
