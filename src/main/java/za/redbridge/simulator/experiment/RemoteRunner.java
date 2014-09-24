package za.redbridge.simulator.experiment;

import za.redbridge.simulator.config.MorphologyConfig;

import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by racter on 2014/09/24.
 */
public class RemoteRunner implements Runnable {

    private Set<MorphologyConfig> complements;
    private String IP;
    private boolean threadComplements;
    private ConcurrentSkipListMap<ComparableMorphology, TreeMap<ComparableNEATNetwork, Integer>> morphologyScores;

    public RemoteRunner(String IP, Set<MorphologyConfig> complements, boolean threadComplements, ConcurrentSkipListMap<ComparableMorphology, TreeMap<ComparableNEATNetwork, Integer>> morphologyScores) {

        this.complements = complements;
        this.IP = IP;
        this.morphologyScores = morphologyScores;

    }

    public void run() {



    }
}
