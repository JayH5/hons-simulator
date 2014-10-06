package za.redbridge.simulator.ea.neat;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.genetic.GeneticError;
import org.encog.neural.NeuralNetworkError;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import za.redbridge.simulator.ea.hetero.CooperativeHeteroNEATNetwork;

import java.io.Serializable;
import java.util.*;

/**
 * Created by racter on 2014/10/05.
 */
public class CCHNEATCODEC implements GeneticCODEC, Serializable {

    /**
     * The serial ID.
     */
    private static final long serialVersionUID = 3L;

    /**
     * {@inheritDoc}
     */
    @Override
    public MLMethod decode(final Genome genome) {
        final NEATGenome neatGenome = (NEATGenome) genome;
        final NEATPopulation pop = (NEATPopulation) neatGenome.getPopulation();
        final List<NEATNeuronGene> neuronsChromosome = neatGenome
                .getNeuronsChromosome();
        final List<NEATLinkGene> linksChromosome = neatGenome
                .getLinksChromosome();

        if (neuronsChromosome.get(0).getNeuronType() != NEATNeuronType.Bias) {
            throw new NeuralNetworkError(
                    "The first neuron must be the bias neuron, this genome is invalid.");
        }

        final List<NEATLink> links = new ArrayList<NEATLink>();
        final ActivationFunction[] afs = new ActivationFunction[neuronsChromosome
                .size()];

        for (int i = 0; i < afs.length; i++) {
            afs[i] = neuronsChromosome.get(i).getActivationFunction();
        }

        final Map<Long, Integer> lookup = new HashMap<Long, Integer>();
        for (int i = 0; i < neuronsChromosome.size(); i++) {
            final NEATNeuronGene neuronGene = neuronsChromosome.get(i);
            lookup.put(neuronGene.getId(), i);
        }

        // loop over connections
        for (int i = 0; i < linksChromosome.size(); i++) {
            final NEATLinkGene linkGene = linksChromosome.get(i);
            if (linkGene.isEnabled()) {
                links.add(new NEATLink(lookup.get(linkGene.getFromNeuronID()),
                        lookup.get(linkGene.getToNeuronID()), linkGene
                        .getWeight()));
            }

        }

        Collections.sort(links);

        final NEATNetwork network = new NEATNetwork(neatGenome.getInputCount(),
                neatGenome.getOutputCount(), links, afs);

        network.setActivationCycles(pop.getActivationCycles());

        final CooperativeHeteroNEATNetwork chNetwork = new CooperativeHeteroNEATNetwork(network);

        return chNetwork;
    }

    /**
     * This method is not currently implemented. If you have need of it, and do
     * implement a conversion from a NEAT phenotype to a genome, consider
     * contribution to the Encog project.
     * @param phenotype Not used.
     * @return Not used.
     */
    @Override
    public Genome encode(final MLMethod phenotype) {
        throw new GeneticError(
                "Encoding of a NEAT network is not supported.");
    }

}

