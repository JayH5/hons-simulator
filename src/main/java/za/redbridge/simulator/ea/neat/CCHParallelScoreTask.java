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

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.exception.EARuntimeError;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.score.AdjustScore;
import org.encog.ml.ea.score.parallel.ParallelScore;
import org.encog.ml.ea.train.basic.BasicEA;
import za.redbridge.simulator.ea.hetero.CCHIndividual;

import java.util.List;

/**
 * An individual threadable task for the parallel score calculation.
 *
 */
public class CCHParallelScoreTask implements Runnable {

    /**
     * The genome to calculate the score for.
     */
    private final CCHIndividual individual;

    /**
     * The score function.
     */
    private final CalculateScore scoreFunction;

    /**
     * The score adjusters.
     */
    private final List<AdjustScore> adjusters;

    /**
     * The owners.
     */
    private final CCHParallelScore owner;

    /**
     * Construct the parallel task.
     * @param individual The genome.
     * @param theOwner The owner.
     */
    public CCHParallelScoreTask(CCHIndividual individual, CCHParallelScore theOwner) {
        super();
        this.owner = theOwner;
        this.individual = individual;
        this.scoreFunction = theOwner.getScoreFunction();
        this.adjusters = theOwner.getAdjusters();
    }

    /**
     * Perform the task.
     */
    @Override
    public void run() {
        MLMethod phenotype = individual;
        if (phenotype != null) {
            double score;
            try {
                score = this.scoreFunction.calculateScore(phenotype);
            } catch(EARuntimeError e) {
                score = Double.NaN;
            }
            individual.getGenome().setScore(score);
            individual.getGenome().setAdjustedScore(score);
            BasicEA.calculateScoreAdjustment(individual.getGenome(), adjusters);
        } else {

        }
    }

}
