package za.redbridge.simulator.ea.hetero;

import org.encog.ml.MLMethod;

import java.util.Set;

/**
 * Created by racter on 2014/10/05.
 */
public class NEATTeam implements MLMethod {

    private final Set<CCHIndividual> team;

    public NEATTeam (Set<CCHIndividual> team) {

        this.team = team;
    }

    public Set<CCHIndividual> getGenotypes() {

        return team;
    }
}
