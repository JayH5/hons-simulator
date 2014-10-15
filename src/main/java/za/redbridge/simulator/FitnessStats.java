package za.redbridge.simulator;

import java.util.HashMap;
import java.util.Map;

import za.redbridge.simulator.phenotype.Phenotype;

/**
 * Created by xenos on 10/8/14.
 */
public class FitnessStats {

    private final Map<Phenotype,Double> phenotypeFitnesses = new HashMap<>();
    private double teamFitness = 0.0;

    private final double totalResourceValue;

    public FitnessStats(double totalResourceValue) {
        this.totalResourceValue = totalResourceValue;
    }

    /**
     * Increment a phenotype's fitness.
     * @param phenotype the phenotype who's score will be adjusted
     * @param adjustedValue the adjusted value of the resource
     */
    public void addToPhenotypeFitness(Phenotype phenotype, double adjustedValue) {
        phenotypeFitnesses.put(phenotype, getPhenotypeFitness(phenotype) + adjustedValue);
    }

    public double getPhenotypeFitness(Phenotype phenotype) {
        return phenotypeFitnesses.getOrDefault(phenotype, 0.0);
    }

    /**
     * Increment the team fitness.
     * @param value the unadjusted resource value
     */
    public void addToTeamFitness(double value) {
        teamFitness += value;
    }

    /** Gets the normalized team fitness (out of 100) */
    public double getTeamFitness() {
        return teamFitness / totalResourceValue * 100;
    }

    public Map<Phenotype,Double> getPhenotypeFitnessMap() {
        return phenotypeFitnesses;
    }

    public double getTotalResourceValue() {
        return totalResourceValue;
    }
}
