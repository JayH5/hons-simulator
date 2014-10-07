package za.redbridge.simulator.ea.neat;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.genome.Genome;
import za.redbridge.simulator.ea.hetero.CCHIndividual;

/**
 * Created by shsu on 2014/10/06.
 */
public class CCHCalculateScore implements CalculateScore {

    //MLMethod should be CCHIndividual which we return the score for.
    @Override
    public double calculateScore(MLMethod method) {
        return ((CCHIndividual) method).getAverageTaskScore();
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
