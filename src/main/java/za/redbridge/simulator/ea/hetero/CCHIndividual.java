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

    private List<Double> cooperativeScores;
    private List<Double> taskScores;
    private final NEATNetwork network;
    //the NEATGenome backing this NEATNetwork.
    private final NEATGenome genome;

    //reference to this individual's team
    private final NEATTeam team;

    public int compareTo(CCHIndividual other) {

        return Double.compare(getTotalTaskScore(),other.getTotalTaskScore());
    }

    public CCHIndividual(final NEATNetwork network, final NEATGenome genome, final NEATTeam team) {

        this.network = network;
        this.genome = genome;
        this.team = team;

        cooperativeScores = Collections.synchronizedList(new ArrayList<>());
        taskScores = Collections.synchronizedList(new ArrayList<>());
    }

    public CCHIndividual() {
        cooperativeScores = Collections.synchronizedList(new ArrayList<>(1));
        taskScores = Collections.synchronizedList(new ArrayList<>(1));

        network = null;
        genome = null;
        team = null;
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

    public NEATTeam getTeam() {
        return team;
    }
}
