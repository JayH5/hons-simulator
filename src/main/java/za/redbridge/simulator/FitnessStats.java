package za.redbridge.simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import za.redbridge.simulator.phenotype.Phenotype;

/**
 * Created by xenos on 10/8/14.
 */
public class FitnessStats {

    private final Map<Phenotype,Double> phenotypeFitnesses = new HashMap<>();
    private double teamFitness = 0.0;

    private final double totalResourceValue;
    private int maxSteps;

    public FitnessStats(double totalResourceValue, int maxSteps) {
        this.totalResourceValue = totalResourceValue;
        this.maxSteps = maxSteps;
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

    /** Gets the normalized team fitness (out of 120 if stepsTaken is provided; out of 100 otherwise) */
    public double getTeamFitness(Optional<Integer> stepsTaken) {
        return (teamFitness / totalResourceValue) * 100 + (stepsTaken.orElse(0) / maxSteps) * 20;
    }

    public Map<Phenotype,Double> getPhenotypeFitnessMap() {
        return phenotypeFitnesses;
    }

    public double getTotalResourceValue() {
        return totalResourceValue;
    }
}
