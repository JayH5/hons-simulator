package za.redbridge.simulator.ea.hetero;

import org.encog.ml.MLMethod;
import za.redbridge.simulator.phenotype.ScoreKeepingController;

import java.util.Set;

/**
 * Created by racter on 2014/10/05.
 * A team of genomes.
 */
public interface Team {

    Set<ScoreKeepingController> getScoreKeepingGenotypes();

    //get the underlying genomes backing this team.
    Set<Object> getGenomes();
}
