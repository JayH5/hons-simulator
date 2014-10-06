package za.redbridge.simulator.ea.neat;

import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATGenomeFactory;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Created by racter on 2014/10/05.
 * Factory for HeteroCooperativeNeatGenomes.
 */
public class HeteroCoopNeatGenomeFactory implements NEATGenomeFactory, Serializable {

    /**
     * The serial ID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * {@inheritDoc}
     */
    @Override
    public NEATGenome factor() {
        return new NEATGenome();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Genome factor(final Genome other) {
        return new NEATGenome((NEATGenome) other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NEATGenome factor(final List<NEATNeuronGene> neurons,
                             final List<NEATLinkGene> links, final int inputCount,
                             final int outputCount) {
        return new NEATGenome(neurons, links, inputCount, outputCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NEATGenome factor(final Random rnd, final NEATPopulation pop,
                             final int inputCount, final int outputCount,
                             final double connectionDensity) {
        return new NEATGenome(rnd, pop, inputCount, outputCount,
                connectionDensity);
    }
}
