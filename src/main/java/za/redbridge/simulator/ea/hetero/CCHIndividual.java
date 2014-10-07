package za.redbridge.simulator.ea.hetero;

import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATGenome;
import za.redbridge.simulator.phenotype.ScoreKeepingController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by racter on 2014/10/05.
 * Wraps a NEAT network with a cooperative score values, as well as a task performance score.
 */

public class CCHIndividual implements MLMethod, ScoreKeepingController, Comparable<CCHIndividual> {

    private List<Double> cooperativeScores;
    private List<Double> taskScores;
    private final NEATNetwork network;
    //the NEATGenome backing this NEATNetwork.
    private final NEATGenome genome;

    public int compareTo(CCHIndividual other) {

        return Double.compare(getTotalTaskScore(),other.getTotalTaskScore());
    }

    public CCHIndividual(final NEATNetwork network, final NEATGenome genome) {

        this.network = network;
        this.genome = genome;

        cooperativeScores = Collections.synchronizedList(new ArrayList<>());
        taskScores = Collections.synchronizedList(new ArrayList<>());

    }

    public CCHIndividual() {
        cooperativeScores = Collections.synchronizedList(new ArrayList<>(1));
        taskScores = Collections.synchronizedList(new ArrayList<>(1));

        network = null;
        genome = null;
    }

    public void incrementTotalCooperativeScore(double input) {

        cooperativeScores.add(input);
    }

    public void incrementTotalTaskScore(double input) {

        taskScores.add(input);
    }

    @Override
    public double getTotalCooperativeScore() {

        double sum = 0;
        synchronized (cooperativeScores) {

            Iterator<Double> it = cooperativeScores.iterator();
            while (it.hasNext()) {
                sum += it.next();
            }
        }

        return sum;
    }

    @Override
    public double getTotalTaskScore() {

        double sum = 0;
        synchronized (taskScores) {

            Iterator<Double> it = taskScores.iterator();
            while (it.hasNext()) {
                sum += it.next();
            }
        }

        return sum;
    }

    @Override
    public double getAverageTaskScore() { return getTotalTaskScore()/taskScores.size(); }

    @Override
    public double getAverageCooperativeScore() { return getTotalCooperativeScore()/cooperativeScores.size(); }

    public NEATNetwork getNetwork() {

        return network;
    }

    public NEATGenome getGenome() {

        return genome;
    }
}
