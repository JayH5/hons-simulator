package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;

/**
 * Created by shsu on 2014/09/15.
 */
//comparable wrapper for NEATNetwork and its associated score
public class ComparableNEATNetwork implements Comparable<ComparableNEATNetwork> {

    private final NEATNetwork network;
    private final double score;

    public ComparableNEATNetwork(NEATNetwork network, double score) {

        this.network = network;
        this.score = score;
    }

    @Override
    public int compareTo(ComparableNEATNetwork o) {
        return Double.compare(score, o.getScore());
    }

    public NEATNetwork getNetwork() { return network; }

    public double getScore() { return score; }

}
