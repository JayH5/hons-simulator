package za.redbridge.simulator.experiment;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.population.Population;

/**
 * Created by racter on 2014/09/21.
 */
public class BasicEA extends org.encog.ml.ea.train.basic.BasicEA {

    BasicEA(final Population thePopulation,
            final CalculateScore theScoreFunction) {
        super(thePopulation, theScoreFunction);
    }



}
