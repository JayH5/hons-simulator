package za.redbridge.simulator.ea.hetero;

import org.encog.ml.MLMethod;

import java.util.Set;

/**
 * Created by racter on 2014/10/05.
 */
public class NEATTeam implements MLMethod, Comparable<NEATTeam> {

    private final Set<CCHIndividual> team;
    private double teamFitness;

    public int compareTo(NEATTeam other) {
        return Double.compare(teamFitness(), other.teamFitness());
    }

    public NEATTeam (Set<CCHIndividual> team) {

        this.team = team;
    }

    public Set<CCHIndividual> getGenotypes() {

        return team;
    }

    public void setTeamFitness(double teamFitness) { this.teamFitness = teamFitness; }

    public double teamFitness() { return teamFitness; }
}
