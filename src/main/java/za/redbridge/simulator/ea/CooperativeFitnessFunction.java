package za.redbridge.simulator.ea;

import za.redbridge.simulator.object.ResourceObject;

/**
 * Created by shsu on 2014/08/29.
 */
public class CooperativeFitnessFunction implements FitnessFunction {

    @Override
    public double calculateFitness(ResourceObject resource) {

        return resource.getNumberPushingRobots();
    }
}
