package za.redbridge.simulator.ea.hetero;

import org.encog.ml.MLMethod;

import java.util.Set;

/**
 * Created by racter on 2014/10/05.
 */
public class NEATTeam implements MLMethod, Comparable<NEATTeam> {

    private final Set<CCHIndividual> team;

    public int compareTo(NEATTeam other) {
        return Double.compare(teamFitness(), other.teamFitness());
    }

    public NEATTeam (Set<CCHIndividual> team) {

        this.team = team;
    }

    public Set<CCHIndividual> getGenotypes() {

        return team;
    }

    public double teamFitness() {

        double output = 0;

        for (CCHIndividual individual: team) {
            output += individual.getAverageTaskScore();
        }

        return output;
    }

}
