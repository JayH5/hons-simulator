package za.redbridge.simulator.ea;

/**
 * Created by shsu on 2014/08/13.
 */
//default fitness function just returns the value given to it
public class DefaultFitnessFunction implements FitnessFunction {

    @Override
    public double calculateFitness(double totalResourceValue) {

        return totalResourceValue;
    }

}
