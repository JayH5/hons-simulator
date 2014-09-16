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

    private MorphologyConfig template;
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

        int numMorphologies = (int) Math.pow(Math.ceil(1 / resolution), numConfigurableSensors);
        System.out.println("Generating " + numMorphologies + " complements.");

        double[] sensitivities = new double[numConfigurableSensors];

        generateAndConfigure(sensitivities, 0, morphologyList);

        System.out.println("Generated " + morphologyList.size() + " complements.");

        for (MorphologyConfig config: morphologyList) {
            printArray(config.getSensitivities());
        }

        return morphologyList;
    }

    public static MorphologyConfig MorphologyFromSensitivities (MorphologyConfig template, double[] sensitivities) {

        ArrayList<AgentSensor> newSensors = new ArrayList<>();
        int counter = 0;

        for (AgentSensor sensor : template.getSensorList()) {
            AgentSensor clone = sensor.clone();

            if (clone instanceof ThresholdedObjectProximityAgentSensor) {

                ((ThresholdedObjectProximityAgentSensor) clone).setSensitivity(sensitivities[counter]);
                counter++;
            } else if (clone instanceof ThresholdedProximityAgentSensor) {

                ((ThresholdedProximityAgentSensor) clone).setSensitivity(sensitivities[counter]);
                counter++;
            }

            newSensors.add(clone);
        }

        return new MorphologyConfig(newSensors);
    }

    public void generateAndConfigure (double[] sensitivities, int currentIndex,
                                      Set<MorphologyConfig> morphologyList) {

        if (currentIndex > sensitivities.length-1) {
            return;
        }

        for (float j = 0; j < (int) (1 / resolution) + 1; j++) {

            sensitivities[currentIndex] = resolution * j;

            //System.out.print("current index: " + currentIndex + " ");
            //printArray(sensitivities);

            morphologyList.add(MorphologyFromSensitivities(template, sensitivities));

            generateAndConfigure(sensitivities, currentIndex+1, morphologyList);
        }
    }

    void printArray(double[] array) {

        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }
}
