package za.redbridge.simulator.ea.hetero;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;

/**
 * Created by racter on 2014/10/05.
 */
public class NNTeamMemberScore implements CalculateScore {

    private final TeamEvaluator teamEvaluator;
    private final CooperativeHeteroNEATNetwork network;

    public NNTeamMemberScore(TeamEvaluator teamEvaluator, CooperativeHeteroNEATNetwork genotype) {

        this.teamEvaluator = teamEvaluator;
        this.network = genotype;
    }

    //the network here must correspond to the CooperativeHeteroNEATNetwork passed into the constructor.
    public double calculateScore(MLMethod method) {

        return network.getAverageTaskScore();
    }

    @Override
    public boolean shouldMinimize() {
        return false;
    }

    @Override
    public boolean requireSingleThreaded() {
        return false;
    }
}
