package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;
import za.redbridge.simulator.config.MorphologyConfig;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by shsu on 2014/09/16.
 */
public class ComparableMorphology implements Comparable<ComparableMorphology> {

    private final MorphologyConfig morphology;
    private final double score;

    public ComparableMorphology(MorphologyConfig morphology, double score) {

        this.morphology = morphology;
        this.score = score;
    }

    @Override
    public int compareTo(ComparableMorphology o) {
        return Double.compare(score, o.getScore());
    }

    public MorphologyConfig getMorphology() { return morphology; }

    public double getScore() { return score; }
}
