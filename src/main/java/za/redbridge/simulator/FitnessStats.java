package za.redbridge.simulator;

import za.redbridge.simulator.object.ResourceObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xenos on 10/8/14.
 */
public class FitnessStats {
    protected double taskFitness;
    protected Set<RetrievedResource> coopFitness;

    public FitnessStats(){
        taskFitness = 0.0;
        coopFitness = new HashSet<>();
    }
    public void setTaskFitness(double taskFitness){
        this.taskFitness = taskFitness;
    }

    public void addTaskFitness(double num){
        taskFitness += num;
    }

    public double getTaskFitness() {
        return taskFitness;
    }

    public Set<RetrievedResource> getCoopFitness() {
        return coopFitness;
    }

    public void addRetrievedResource(ResourceObject o, int numRobots){
        coopFitness.add(new RetrievedResource(o, numRobots));
    }

    protected class RetrievedResource{
        public ResourceObject resource;
        public int numRobots;

        public RetrievedResource(ResourceObject resource, int numRobots){
            this.resource = resource;
            this.numRobots = numRobots;
        }
    }
}
