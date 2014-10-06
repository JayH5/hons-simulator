package za.redbridge.simulator.ea.hetero;

import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;

import java.util.List;
import java.util.Set;

/**
 * Created by racter on 2014/10/05.
 */
public class NEATTeam implements MLMethod {

    private final Set<CooperativeHeteroNEATNetwork> team;

    public NEATTeam (Set<CooperativeHeteroNEATNetwork> team) {

        this.team = team;
    }

    public Set<CooperativeHeteroNEATNetwork> getGenotypes() {

        return team;
    }
}
