package za.redbridge.simulator.ea.neat;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.neat.training.opp.NEATMutateAddLink;
import org.encog.neural.neat.training.opp.NEATMutateAddNode;
import org.encog.neural.neat.training.opp.NEATMutateRemoveLink;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.hetero.CCHIndividual;
import za.redbridge.simulator.experiment.ComparableMorphology;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by racter on 2014/09/21.
 */
public class CNNEATUtil {

    /**
     * Construct a NEAT (or HyperNEAT trainer.
     * @param population The population.
     * @param calculateScore The score function.
     * @return The NEAT EA trainer.
     */
    public static CCHNEATTrainer constructCCHNEATTrainer(final CCHNEATPopulation population,
                                               final CalculateScore calculateScore,
                                               final ExperimentConfig experimentConfig, final SimConfig simConfig,
                                               final MorphologyConfig morphologyConfig) {

        final CCHNEATTrainer result = new CCHNEATTrainer(population, calculateScore, experimentConfig,
                simConfig, morphologyConfig);

        result.setSpeciation(new OriginalNEATSpeciation());
        result.setSelection(new TruncationSelection(result, 0.3));
        result.setMaxOperationErrors(0);
        final CompoundOperator weightMutation = new CompoundOperator();
        weightMutation.getComponents().add(
                0.1125,
                new CCHNEATMutateWeights(new SelectFixed(1),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new CCHNEATMutateWeights(new SelectFixed(2),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new CCHNEATMutateWeights(new SelectFixed(3),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new CCHNEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new CCHNEATMutateWeights(new SelectFixed(1),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new CCHNEATMutateWeights(new SelectFixed(2),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new CCHNEATMutateWeights(new SelectFixed(3),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new CCHNEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.03,
                new CCHNEATMutateWeights(new SelectFixed(1),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.03,
                new CCHNEATMutateWeights(new SelectFixed(2),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.03,
                new CCHNEATMutateWeights(new SelectFixed(3),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.01,
                new CCHNEATMutateWeights(new SelectProportion(0.02),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().finalizeStructure();
        result.setChampMutation(weightMutation);
        result.addOperation(0.5, new CNNeatCrossover());
        result.addOperation(0.5, weightMutation);
        result.addOperation(0.001, new CCHNEATMutateAddNode());
        result.addOperation(0.005, new CCHNEATMutateAddLink());
        result.addOperation(0.0005, new NEATMutateRemoveLink());
        result.getOperators().finalizeStructure();
        if (population.isHyperNEAT()) {
            result.setCODEC(new HyperNEATCODEC());
        } else {
            result.setCODEC(new CCHNEATCODEC());
        }
        return result;
    }
}
