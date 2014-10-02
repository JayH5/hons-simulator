package za.redbridge.simulator.ea;

import za.redbridge.simulator.object.ResourceObject;

/**
 * Created by shsu on 2014/08/13.
 */
//default fitness function just returns area of the resource
public class DefaultFitnessFunction implements FitnessFunction {

    @Override
    public double calculateFitness(ResourceObject resource) {

        return resource.getWidth() * resource.getHeight();
    }
}
