package za.redbridge.simulator.ea.hetero;

import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;
import za.redbridge.simulator.phenotype.ScoreKeepingController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by racter on 2014/10/05.
 * Wraps a NEAT network with a cooperative score values, as well as a task performance score.
 */

public class CooperativeHeteroNEATNetwork implements MLMethod, ScoreKeepingController {

    private List<Double> cooperativeScores;
    private List<Double> taskScores;
    private NEATNetwork network;

    public CooperativeHeteroNEATNetwork (NEATNetwork network) {

        this.network = network;
        cooperativeScores = Collections.synchronizedList(new ArrayList<>());
        taskScores = Collections.synchronizedList(new ArrayList<>());
    }

    public synchronized void incrementTotalCooperativeScore(double input) {

        cooperativeScores.add(input);
    }

    public synchronized void incrementTotalTaskScore(double input) {

        taskScores.add(input);
    }

    public double getAverageTaskScore() {

        double sum = 0;
        double num = 1;

        synchronized (cooperativeScores) {

            Iterator<Double> it = cooperativeScores.iterator();
            while (it.hasNext()) {
                sum += it.next();
            }

            num = cooperativeScores.size();
        }

        return sum/num;
    }

    public double getAverageCooperativeScore() {

        double sum = 0;
        double num = 1;

        synchronized (taskScores) {

            Iterator<Double> it = taskScores.iterator();
            while (it.hasNext()) {
                sum += it.next();
            }

            num = taskScores.size();
        }

        return sum/num;
    }

    public NEATNetwork getNetwork() {

        return network;
    }
}
