package za.redbridge.simulator.factories;

import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.ea.SensitivityGenome;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ThresholdedObjectProximityAgentSensor;
import za.redbridge.simulator.sensor.ThresholdedProximityAgentSensor;

import java.util.ArrayList;
import java.util.List;

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

    public List<MorphologyConfig> generateComplementsForTemplate() {

        ArrayList<MorphologyConfig> morphologyList = new ArrayList<>();

        int numConfigurableSensors = 0;

        for (AgentSensor sensor: template.getSensorList()) {

            if (sensor instanceof ThresholdedObjectProximityAgentSensor
                    || sensor instanceof ThresholdedProximityAgentSensor) {
                numConfigurableSensors++;
            }
        }

        int numMorphologies = (int) Math.pow(Math.ceil(1 / resolution), numConfigurableSensors);
        System.out.println("Generating " + numMorphologies + " morphologies.");

        double[] sensitivities = new double[numConfigurableSensors];

        generateAndConfigure(sensitivities, 0, 0, morphologyList);

        System.out.println("Generated " + morphologyList.size() + " morphologies.");

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

    public void generateAndConfigure (double[] sensitivities, int index, float valueAtIndex,
                                      ArrayList<MorphologyConfig> morphologyList) {

        int ix = index;
        float vix = valueAtIndex;

        if (valueAtIndex+resolution > 1) {
            index++;
            vix = 0;
        }

        if (index > sensitivities.length-1) {
            return;
        }

        sensitivities[ix] = vix;

        for (int i = 0; i < sensitivities.length; i++) {

            if (i == index) {
                continue;
            }
                for (float j = 0; j < (int) (1 / resolution); j++) {

                    sensitivities[i] = resolution * j;
                    printArray(sensitivities);

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

                    morphologyList.add(new MorphologyConfig(newSensors));
                }
        }

        generateAndConfigure(sensitivities, ix, vix+resolution, morphologyList);

    }

    void printArray(double[] array) {

        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }
}
