package za.redbridge.simulator.ea;

import org.encog.ml.genetic.genome.DoubleArrayGenome;

/**
 * Created by shsu on 2014/09/16.
 */
public class SensitivityGenome extends DoubleArrayGenome {

    public SensitivityGenome(SensitivityGenome other) {
        super(other);
    }

    public SensitivityGenome(int size) {
        super(size);
    }

}
