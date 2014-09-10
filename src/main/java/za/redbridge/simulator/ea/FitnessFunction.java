package za.redbridge.simulator.ea;

import za.redbridge.simulator.object.ResourceObject;

/**
 * Created by shsu on 2014/08/13.
 */
//fitness function interface for foraging task
public interface FitnessFunction {

    //this isn't particularly flexible atm...originally had an instance of TargetAreaObject as parameter
    public double calculateFitness (ResourceObject resource);

}
