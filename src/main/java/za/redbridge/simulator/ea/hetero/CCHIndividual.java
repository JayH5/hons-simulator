package za.redbridge.simulator.ea.hetero;

import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATGenome;
import za.redbridge.simulator.phenotype.ScoreKeepingController;

import java.util.*;

/**
 * Created by racter on 2014/10/05.
 * Wraps a NEAT network with a cooperative score values, as well as a task performance score.
 */

public class CCHIndividual implements MLMethod, ScoreKeepingController, Comparable<CCHIndividual> {

    //scores in a single run
    private double taskScore;

    //scores across all runs
    private final List<Double> allTaskScores;

    private final NEATNetwork network;
    //the NEATGenome backing this NEATNetwork.
    private final NEATGenome genome;

    //reference to this individual's team
    private final NEATTeam team;

    public int compareTo(CCHIndividual other) {

        return Double.compare(getAverageTaskScore(),other.getAverageTaskScore());
    }

    public CCHIndividual(final NEATNetwork network, final NEATGenome genome, final NEATTeam team) {

        this.network = network;
        this.genome = genome;
        this.team = team;

        allTaskScores = Collections.synchronizedList(new ArrayList<Double>());
    }

    public CCHIndividual() {
        allTaskScores = Collections.synchronizedList(new ArrayList<Double>());
        network = null;
        genome = null;
        team = null;
    }


    @Override
    public void addTaskScore(double input) { allTaskScores.add(input); }

    @Override
    public double getAverageTaskScore() {

        double sum = 0;
        double num = 1;
        synchronized (allTaskScores) {

            Iterator<Double> it = allTaskScores.iterator();
            while (it.hasNext()) {
                sum += it.next();
            }
        }

        return sum/num;
    }

    public NEATGenome getGenome() {

        return genome;
    }

    public NEATNetwork getNetwork() { return network; }

    public NEATTeam getTeam() {
        return team;
    }
}