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
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.population.Population;
import org.encog.ml.ea.score.AdjustScore;
import org.encog.ml.ea.score.parallel.ParallelScoreTask;
import org.encog.ml.ea.species.Species;
import org.encog.ml.genetic.GeneticError;
import org.encog.util.concurrency.MultiThreadable;
import za.redbridge.simulator.ea.hetero.CCHIndividual;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to calculate the scores for an entire population. This is
 * typically done when a new population must be scored for the first time.
 */
public class CCHParallelScore implements MultiThreadable {
    /**
     * The population to score.
     */
    private final Population population;


    /**
     * The scoring function.
     */
    private final CalculateScore scoreFunction;

    /**
     * The score adjuster.
     */
    private final List<AdjustScore> adjusters;

    /**
     * The number of requested threads.
     */
    private int threads;

    /**
     * The actual number of threads.
     */
    private int actualThreads;

    private final Set<CCHIndividual> individuals;

    /**
     * Construct the parallel score calculation object.
     * @param thePopulation The population to score.
     * @param theAdjusters The score adjusters to use.
     * @param theScoreFunction The score function.
     * @param theThreadCount The requested thread count.
     */
    public CCHParallelScore(Population thePopulation, Set<CCHIndividual> individuals,
                         List<AdjustScore> theAdjusters, CalculateScore theScoreFunction,
                         int theThreadCount) {
        this.population = thePopulation;
        this.scoreFunction = theScoreFunction;
        this.adjusters = theAdjusters;
        this.actualThreads = 0;
        this.individuals = individuals;
    }

    /**
     * @return the population
     */
    public Population getPopulation() {
        return population;
    }

    /**
     * @return the scoreFunction
     */
    public CalculateScore getScoreFunction() {
        return scoreFunction;
    }


    /**
     * Calculate the scores.
     */
    public void process() {
        // determine thread usage
        if (this.scoreFunction.requireSingleThreaded()) {
            this.actualThreads = 1;
        } else if (threads == 0) {
            this.actualThreads = Runtime.getRuntime().availableProcessors();
        } else {
            this.actualThreads = threads;
        }

        // start up
        ExecutorService taskExecutor = null;

        if (this.threads == 1) {
            taskExecutor = Executors.newSingleThreadScheduledExecutor();
        } else {
            taskExecutor = Executors.newFixedThreadPool(this.actualThreads);
        }

        for (CCHIndividual i: individuals) {
            taskExecutor.execute(new CCHParallelScoreTask(i, this));
        }

        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new GeneticError(e);
        }
    }

    /**
     * @return The score adjusters.
     */
    public List<AdjustScore> getAdjusters() {
        return this.adjusters;
    }

    /**
     * @return The desired number of threads.
     */
    @Override
    public int getThreadCount() {
        return this.threads;
    }

    /**
     * @param numThreads The desired thread count.
     */
    @Override
    public void setThreadCount(int numThreads) {
        this.threads = numThreads;
    }
}
