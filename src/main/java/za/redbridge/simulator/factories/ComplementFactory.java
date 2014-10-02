package za.redbridge.simulator.factories;

import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.ea.SensitivityGenome;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ThresholdedObjectProximityAgentSensor;
import za.redbridge.simulator.sensor.ThresholdedProximityAgentSensor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shsu on 2014/09/15.
 */
//generates MorphologyConfig objects and varies sensitivities - will overwrite templates
public class ComplementFactory {

    private final MorphologyConfig template;
    private float resolution;

    public ComplementFactory(MorphologyConfig template, float resolution) {

        this.template = template;

        if (resolution > 1) {
            System.out.println("Generator resolution is too large.");
            System.exit(-1);
        }
        if (resolution <= 0.01) {
            System.out.println("Generator resolution is too small.");
            System.exit(-1);
        }

        this.resolution = resolution;
    }

    public Set<MorphologyConfig> generateComplementsForTemplate() {

        Set<MorphologyConfig> morphologyList = new HashSet<>();
        int numConfigurableSensors = template.getNumAdjustableSensitivities();

        double[] sensitivities = new double[numConfigurableSensors];

        for (int i = 0; i < sensitivities.length; i++) {
            sensitivities[i] = resolution;
        }

        generateAndConfigure(sensitivities, 0, morphologyList);

        return morphologyList;
    }

    public void generateAndConfigure (double[] sensitivities, int currentIndex,
                                      Set<MorphologyConfig> morphologyList) {

        if (currentIndex > sensitivities.length-1) {
            return;
        }

        for (float j = 1; j < (int) (1 / resolution) + 1; j++) {

            sensitivities[currentIndex] = resolution * j;
            morphologyList.add(MorphologyConfig.MorphologyFromSensitivities(template, sensitivities));
            generateAndConfigure(sensitivities, currentIndex+1, morphologyList);
        }
    }

    public static void printArray(double[] array) {

        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }
}
