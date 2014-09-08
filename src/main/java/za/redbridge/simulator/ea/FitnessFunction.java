package za.redbridge.simulator.ea;

import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.TargetAreaObject;

import java.lang.annotation.Target;

/**
 * Created by shsu on 2014/08/13.
 */
//fitness function interface for foraging task
public interface FitnessFunction {

    //this isn't particularly flexible atm...originally had an instance of TargetAreaObject as parameter
    public double calculateFitness (ResourceObject resource);

}
