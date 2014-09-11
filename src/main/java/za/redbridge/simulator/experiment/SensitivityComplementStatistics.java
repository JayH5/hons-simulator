package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by racter on 2014/09/11.
 */
public class SensitivityComplementStatistics {

    private final ConcurrentSkipListMap<NEATNetwork,Double> scores;

    public SensitivityComplementStatistics() {

        scores = new ConcurrentSkipListMap<>();
    }

    public void addScore(NEATNetwork network, double score) {

        scores.put(network, score);
    }
}
