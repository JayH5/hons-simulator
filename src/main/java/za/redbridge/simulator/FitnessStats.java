package za.redbridge.simulator;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.phenotype.Phenotype;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by xenos on 10/8/14.
 */
public class FitnessStats {
    protected Map<Phenotype,Double> phenotypeFitnesses;
    protected double teamFitness;

    public FitnessStats(){
        phenotypeFitnesses = new HashMap<>();
        teamFitness = 0.0;
    }

    public void setPhenotypeFitness(Phenotype p, double num){
        phenotypeFitnesses.put(p, num);
    }

    public void addToPhenotypeFitness(Phenotype p, double num){
        phenotypeFitnesses.put(p, phenotypeFitnesses.getOrDefault(p, 0.0) + num);
    }

    public double getPhenotypeFitness(Phenotype p) {
        return phenotypeFitnesses.getOrDefault(p, 0.0);
    }

    public void setTeamFitness(double f){
        teamFitness = f;
    }

    public void addToTeamFitness(double num){
        teamFitness += num;
    }

    public double getTeamFitness() {
        return teamFitness;
    }

    public Map<Phenotype,Double> getPhenotypeFitnessMap(){
        return phenotypeFitnesses;
    }
}
