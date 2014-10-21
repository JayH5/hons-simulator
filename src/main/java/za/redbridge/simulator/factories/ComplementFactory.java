package za.redbridge.simulator.factories;

import za.redbridge.simulator.config.MorphologyConfig;

import java.util.HashSet;
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

    public Set<MorphologyConfig> generateSensitivitiesForTemplate() {

        Set<MorphologyConfig> morphologyList = new HashSet<>();
        double[] sensitivities = new double[4];

        for (int i = 0; i < sensitivities.length; i++) {
            sensitivities[i] = resolution;
        }

        generateAndConfigureSensitivities(sensitivities, 0, morphologyList);

        System.out.println("Generated " + morphologyList.size() + " complements.");

        return morphologyList;
    }

    public void generateAndConfigureSensitivities (double[] sensitivities, int currentIndex,
                                      Set<MorphologyConfig> morphologyList) {

        if (currentIndex > sensitivities.length-1) {
            return;
        }

        for (float j = 1; j < (int) (1.5 / resolution) + 1; j++) {

            sensitivities[currentIndex] = resolution * j;
            morphologyList.add(MorphologyConfig.MorphologyFromGain(template, sensitivities));
            generateAndConfigureSensitivities(sensitivities, currentIndex+1, morphologyList);
        }
    }

    public static void printArray(double[] array) {

        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }
}
