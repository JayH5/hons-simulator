package za.redbridge.simulator.ea.neat;

/**
 * Created by racter on 2014/10/05.
 * Breeds some genomes and adds it to the intermediate population for teaming.
 */

import org.encog.EncogError;
import org.encog.ml.ea.exception.EARuntimeError;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.species.Species;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * A worker thread for an Evolutionary Algorithm. Does not evaluate child immediately after creation.
 */
public class CCHEABreeder implements Callable<Object> {

    /**
     * The species being processed.
     */
    private final Species species;

    /**
     * The parent genomes.
     */
    private final Genome[] parents;

    /**
     * The children genomes.
     */
    private final Genome[] children;

    /**
     * Random number generator.
     */
    private final Random rnd;

    /**
     * The parent object.
     */
    private final CCHBasicEA train;

    /**
     * Construct the EA worker.
     *  @param theTrain
     *            The trainer.
     * @param theSpecies
     */
    public CCHEABreeder(final CCHBasicEA theTrain, final Species theSpecies) {
        this.train = theTrain;
        this.species = theSpecies;
        this.rnd = this.train.getRandomNumberFactory().factor();

        this.parents = new Genome[this.train.getOperators().maxParents()];
        this.children = new Genome[this.train.getOperators().maxOffspring()];
    }

    /**
     * Choose a parent.
     *
     * @return The chosen parent.
     */
    private Genome chooseParent() {
        final int idx = this.train.getSelection().performSelection(this.rnd,
                this.species);
        return this.species.getMembers().get(idx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object call() {
        boolean success = false;
        int tries = this.train.getMaxOperationErrors();
        do {
            try {
                // choose an evolutionary operation (i.e. crossover or a type of
                // mutation) to use
                final EvolutionaryOperator opp = this.train.getOperators()
                        .pickMaxParents(this.rnd,
                                this.species.getMembers().size());

                this.children[0] = null;

                // prepare for either sexual or asexual reproduction either way,
                // we
                // need at least
                // one parent, which is the first parent.
                //
                // Chose the first parent, there must be at least one genome in
                // this
                // species
                this.parents[0] = chooseParent();

                // if the number of individuals in this species is only
                // one then we can only clone and perhaps mutate, otherwise use
                // the crossover probability to determine if we are to use
                // sexual reproduction.
                if (opp.parentsNeeded() > 1) {

                    int numAttempts = 5;

                    this.parents[1] = chooseParent();
                    while (this.parents[0] == this.parents[1]
                            && numAttempts-- > 0) {
                        this.parents[1] = chooseParent();
                    }

                    // success, perform crossover
                    if (this.parents[0] != this.parents[1]) {
                        opp.performOperation(this.rnd, this.parents, 0,
                                this.children, 0);
                    }
                } else {
                    // clone a child (asexual reproduction)
                    opp.performOperation(this.rnd, this.parents, 0,
                            this.children, 0);
                    this.children[0].setPopulation(this.parents[0]
                            .getPopulation());
                }


                // process the new child
                for (Genome child : this.children) {
                    if (child != null) {
                        child.setPopulation(this.parents[0].getPopulation());
                        if (this.train.getRules().isValid(child)) {
                            child.setBirthGeneration(this.train.getIteration());

                            //add it to the intermediate (yet to be teamed) population
                            if (!this.train.addChildToTemp(child)) {
                                return null;
                            }
                            success = true;
                        }
                    }
                }
            } catch (EARuntimeError e) {
                tries--;
                if (tries < 0) {
                    throw new EncogError(
                            "Could not perform a successful genetic operaton after "
                                    + this.train.getMaxOperationErrors()
                                    + " tries.");
                }
            } catch (final Throwable t) {
                if (!this.train.getShouldIgnoreExceptions()) {
                    this.train.reportError(t);
                }
            }

        } while (!success);
        return null;
    }
}
