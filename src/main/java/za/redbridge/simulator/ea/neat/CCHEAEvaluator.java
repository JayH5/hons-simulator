package za.redbridge.simulator.ea.neat;

/**
 * Created by shsu on 2014/10/06.
 */
/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */

import org.encog.EncogError;
import org.encog.ml.ea.exception.EARuntimeError;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.basic.BasicEA;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * A worker thread for an Evolutionary Algorithm.
 */
public class CCHEAEvaluator implements Callable<Object> {

    /**
     * The species being processed.
     */
    private final Species species;

    /**
     * The temporary children whose task fitness in teams has been evaluated
     */
    private final List<Genome> processedChildren;

    /**
     * Random number generator.
     */
    private final Random rnd;

    /**
     * The parent object.
     */
    private final BasicEA train;

    /**
     * Construct the EA worker.
     *
     * @param theTrain
     *            The trainer.
     * @param theSpecies
     *            The species.
     */
    public CCHEAEvaluator(final CCHBasicEA theTrain, final Species theSpecies) {
        this.train = theTrain;
        this.species = theSpecies;
        this.rnd = this.train.getRandomNumberFactory().factor();

        this.processedChildren = theTrain.getTeamPopulation();
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

                // process the new child
                for (Genome child : this.processedChildren) {
                    if (child != null) {
                        if (this.train.getRules().isValid(child)) {

                            this.train.calculateScore(child);

                            if (!this.train.addChild(child)) {
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
