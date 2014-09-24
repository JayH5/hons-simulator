package za.redbridge.simulator.factories;

import ec.util.MersenneTwisterFast;
import org.encog.ml.genetic.genome.DoubleArrayGenomeFactory;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.ea.SensitivityGenome;

/**
 * Created by shsu on 2014/09/16.
 */
public class SensitivityGenomeFactory extends DoubleArrayGenomeFactory {

    private MorphologyConfig template;

    public SensitivityGenomeFactory(int size) {
        super(size);
    }

    public SensitivityGenomeFactory(MorphologyConfig morphologyConfig) {
        super(morphologyConfig.getNumAdjustableSensitivities());
        template = morphologyConfig;
    }

    public SensitivityGenome randomGenome() {

        SensitivityGenome result = new SensitivityGenome(template.getNumAdjustableSensitivities());
        final double organism[] = result.getData();

        MersenneTwisterFast random = new MersenneTwisterFast();

        for (int i = 0; i < organism.length - 1; i++) {

            organism[i] = random.nextDouble(true, true);
        }
        return result;
    }

}
