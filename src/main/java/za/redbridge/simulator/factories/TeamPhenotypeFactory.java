package za.redbridge.simulator.factories;

import za.redbridge.simulator.phenotype.Phenotype;

import java.util.Set;

/**
 * Created by racter on 2014/10/05.
 * Fixed-morphology, Variable controller team factory - does the job of wrapping a team of
 * ScoreKeepingPhenotypes into a team of Phenotypes.
 */
public interface TeamPhenotypeFactory {

    Set<Phenotype> generatePhenotypeTeam();
}
