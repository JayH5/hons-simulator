package za.redbridge.simulator.experiment;

/**
 * Created by shsu on 2014/10/21.
 */
public class NEATPopulation extends org.encog.neural.neat.NEATPopulation {

    public NEATPopulation(final int inputCount, final int outputCount, final int populationSize) {
        super(inputCount, outputCount, populationSize);
    }

    @Override
    public double getWeightRange() {
        return 1;
    }
}
