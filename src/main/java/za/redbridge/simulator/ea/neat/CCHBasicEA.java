package za.redbridge.simulator.ea.neat;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.encog.Encog;
import org.encog.EncogError;
import org.encog.EncogShutdownTask;
import org.encog.mathutil.randomize.factory.RandomFactory;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLContext;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.codec.GenomeAsPhenomeCODEC;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.opp.OperationList;
import org.encog.ml.ea.opp.selection.SelectionOperator;
import org.encog.ml.ea.opp.selection.TournamentSelection;
import org.encog.ml.ea.population.Population;
import org.encog.ml.ea.rules.BasicRuleHolder;
import org.encog.ml.ea.rules.RuleHolder;
import org.encog.ml.ea.score.AdjustScore;
import org.encog.ml.ea.sort.*;
import org.encog.ml.ea.species.SingleSpeciation;
import org.encog.ml.ea.species.Speciation;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.ea.train.basic.BasicEA;
import org.encog.ml.genetic.GeneticError;
import org.encog.util.concurrency.MultiThreadable;
import org.encog.util.logging.EncogLogging;
import org.jbox2d.common.MathUtils;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.hetero.CCHIndividual;
import za.redbridge.simulator.ea.hetero.NEATTeam;
import za.redbridge.simulator.ea.hetero.TeamEvaluator;
import za.redbridge.simulator.experiment.Main;
import za.redbridge.simulator.factories.ComplementFactory;
import za.redbridge.simulator.factories.NEATTeamFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by racter on 2014/10/05.
 */

/**
 * Provides a basic implementation of a multi-threaded Evolutionary Algorithm.
 * The EA works from a score function.
 */
public class CCHBasicEA extends BasicEA implements EvolutionaryAlgorithm, MultiThreadable,
        EncogShutdownTask, Serializable {

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Calculate the score adjustment, based on adjusters.
     *
     * @param genome
     *            The genome to adjust.
     * @param adjusters
     *            The score adjusters.
     */
    public static void calculateScoreAdjustment(final Genome genome,
                                                final List<AdjustScore> adjusters) {
        final double score = genome.getScore();
        double delta = 0;

        for (final AdjustScore a : adjusters) {
            delta += a.calculateAdjustment(genome);
        }

        genome.setAdjustedScore(score + delta);
    }

    /**
     * Should exceptions be ignored.
     */
    private boolean ignoreExceptions;

    /**
     * The genome comparator.
     */
    private GenomeComparator bestComparator;

    /**
     * The genome comparator.
     */
    private GenomeComparator selectionComparator;

    /**
     * The population.
     */
    private CCHNEATPopulation population;

    /**
     * The score calculation function.
     */
    private final CalculateScore scoreFunction;

    /**
     * The selection operator.
     */
    private SelectionOperator selection;

    /**
     * The score adjusters.
     */
    private final List<AdjustScore> adjusters = new ArrayList<AdjustScore>();

    /**
     * The operators. to use.
     */
    private final OperationList operators = new OperationList();

    /**
     * The CODEC to use to convert between genome and phenome.
     */
    private GeneticCODEC codec = new GenomeAsPhenomeCODEC();

    /**
     * Random number factory.
     */
    private RandomFactory randomNumberFactory = Encog.getInstance()
            .getRandomFactory().factorFactory();

    /**
     * The validation mode.
     */
    private boolean validationMode;

    /**
     * The iteration number.
     */
    private int iteration;

    /**
     * The desired thread count.
     */
    private int threadCount;

    /**
     * The actual thread count.
     */
    private int actualThreadCount = -1;

    /**
     * The speciation method.
     */
    private Speciation speciation = new SingleSpeciation();

    /**
     * This property stores any error that might be reported by a thread.
     */
    private Throwable reportedError;

    /**
     * The best genome from the last iteration.
     */
    private Genome oldBestGenome;

    /**
     * The temp population for the next iteration, to be formed into teams
     */
    private final List<Genome> teamPopulation = new ArrayList<Genome>();

    /**
     * The population for the next iteration.
     */
    private final List<Genome> newPopulation = new ArrayList<Genome>();

    /**
     * The mutation to be used on the top genome. We want to only modify its
     * weights.
     */
    private EvolutionaryOperator champMutation;

    /**
     * The percentage of a species that is "elite" and is passed on directly.
     */
    private double eliteRate = 0.3;

    /**
     * The number of times to try certian operations, so an endless loop does
     * not occur.
     */
    private int maxTries = 5;

    /**
     * The best ever genome.
     */
    private Genome bestGenome;

    /**
     * The thread pool executor.
     */
    private ExecutorService taskExecutor;

    /**
     * Holds the threads used each iteration.
     */
    private final List<Callable<Object>> threadList = new ArrayList<Callable<Object>>();

    /**
     * Holds rewrite and constraint rules.
     */
    private RuleHolder rules;

    private CCHIndividual bestIndividual;
    private CCHIndividual currentBestIndividual;

    private NEATTeam bestTeam;
    private NEATTeam currentBestTeam;

    //current average score for this epoch
    private double currentAverage = -1;

    private final ExperimentConfig experimentConfig;
    private final SimConfig simConfig;
    private final MorphologyConfig morphologyConfig;

    //stats stuff

    private double[] lastEpochScores;
    private double[] thisEpochScores;

    private int maxOperationErrors = 0;

    /**
     * Construct an EA.
     *
     * @param thePopulation
     *            The population.
     * @param theScoreFunction
     *            The score function.
     */
    public CCHBasicEA(final CCHNEATPopulation thePopulation,
                      final CalculateScore theScoreFunction, final ExperimentConfig experimentConfig,
                      final SimConfig simConfig, final MorphologyConfig morphologyConfig) {

        super(thePopulation, theScoreFunction);
        this.population = thePopulation;
        this.scoreFunction = theScoreFunction;
        this.selection = new TournamentSelection(this, 4);
        this.rules = new BasicRuleHolder();
        this.experimentConfig = experimentConfig;
        this.simConfig = simConfig;
        this.morphologyConfig = morphologyConfig;

        // set the score compare method
        if (theScoreFunction.shouldMinimize()) {
            this.selectionComparator = new MinimizeAdjustedScoreComp();
            this.bestComparator = new MinimizeScoreComp();
        } else {
            this.selectionComparator = new MaximizeAdjustedScoreComp();
            this.bestComparator = new MaximizeScoreComp();
        }

        // set the iteration
        for (final Species species : thePopulation.getSpecies()) {
            for (final Genome genome : species.getMembers()) {
                setIteration(Math.max(getIteration(),
                        genome.getBirthGeneration()));
            }
        }

        // Set a best genome, just so it is not null.
        // We won't know the true best genome until the first iteration.
        if( this.population.getSpecies().size()>0 && this.population.getSpecies().get(0).getMembers().size()>0 ) {
            this.bestGenome = this.population.getSpecies().get(0).getMembers().get(0);
        }
    }

    /**
     * Add a child to the population (list of genomes) that will be evaluated and scored.
     * @param genome
     *            The child.
     * @return True, if the child was added successfully.
     */
    public boolean addChildToTemp(final Genome genome) {
        synchronized (this.teamPopulation) {
            if (this.teamPopulation.size() < population.getPopulationSize()) {
                // don't read the old best genome, it was already added
                if (genome != this.oldBestGenome) {

                    if (isValidationMode()) {
                        if (this.teamPopulation.contains(genome)) {
                            throw new EncogError(
                                    "Genome already added to population: "
                                            + genome.toString());
                        }
                    }

                    this.teamPopulation.add(genome);
                }

                if (!Double.isInfinite(genome.getScore())
                        && !Double.isNaN(genome.getScore())) {

                    //error checking goes here.

                }
                return true;
            } else {
                return false;
            }
        }
    }


    /**
     * Add a child to the next iteration.
     *
     * @param genome
     *            The child.
     * @return True, if the child was added successfully.
     */
    public boolean addChild(final Genome genome) {
        synchronized (this.newPopulation) {
            if (this.newPopulation.size() < population.getPopulationSize()) {
                // don't read the old best genome, it was already added
                if (genome != this.oldBestGenome) {

                    if (isValidationMode()) {
                        if (this.newPopulation.contains(genome)) {
                            throw new EncogError(
                                    "Genome already added to population: "
                                            + genome.toString());
                        }
                    }

                    this.newPopulation.add(genome);
                }

                if (!Double.isInfinite(genome.getScore())
                        && !Double.isNaN(genome.getScore())
                        && getBestComparator().isBetterThan(genome,
                        this.bestGenome)) {
                    this.bestGenome = genome;
                    population.setBestGenome(this.bestGenome);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOperation(final double probability,
                             final EvolutionaryOperator opp) {
        getOperators().add(probability, opp);
        opp.init(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addScoreAdjuster(final AdjustScore scoreAdjust) {
        this.adjusters.add(scoreAdjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void calculateScore(final Genome g) {

        // try rewrite
        this.rules.rewrite(g);

        // decode
        final MLMethod phenotype = getCODEC().decode(g);
        double score;

        // deal with invalid decode
        if (phenotype == null) {
            if (getBestComparator().shouldMinimize()) {
                score = Double.POSITIVE_INFINITY;
            } else {
                score = Double.NEGATIVE_INFINITY;
            }
        } else {
            if (phenotype instanceof MLContext) {
                ((MLContext) phenotype).clearContext();
            }

            //calculate the score for one genotype
            score = getScoreFunction().calculateScore(phenotype);
        }

        // now set the scores
        g.setScore(score);
        g.setAdjustedScore(score);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishTraining() {

        // wait for threadpool to shutdown
        if (this.taskExecutor != null) {
            this.taskExecutor.shutdown();
            try {
                this.taskExecutor.awaitTermination(Long.MAX_VALUE,
                        TimeUnit.MINUTES);
            } catch (final InterruptedException e) {
                throw new GeneticError(e);
            } finally {
                this.taskExecutor = null;
                Encog.getInstance().removeShutdownTask(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenomeComparator getBestComparator() {
        return this.bestComparator;
    }

    /**
     * @return the bestGenome
     */
    @Override
    public Genome getBestGenome() {
        return this.bestGenome;
    }

    /**
     * @return the champMutation
     */
    public EvolutionaryOperator getChampMutation() {
        return this.champMutation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneticCODEC getCODEC() {
        return this.codec;
    }

    /**
     * @return the eliteRate
     */
    public double getEliteRate() {
        return this.eliteRate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getError() {
        // do we have a best genome, and does it have an error?
        if (this.bestGenome != null) {
            double err = this.bestGenome.getScore();
            if( !Double.isNaN(err) ) {
                return err;
            }
        }

        // otherwise, assume the worst!
        if (getScoreFunction().shouldMinimize()) {
            return Double.POSITIVE_INFINITY;
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIteration() {
        return this.iteration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxIndividualSize() {
        return this.population.getMaxIndividualSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxTries() {
        return this.maxTries;
    }

    /**
     * @return the oldBestGenome
     */
    public Genome getOldBestGenome() {
        return this.oldBestGenome;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationList getOperators() {
        return this.operators;
    }

    /**
     * @return The population.
     */
    @Override
    public Population getPopulation() {
        return this.population;
    }

    /**
     * @return the randomNumberFactory
     */
    public RandomFactory getRandomNumberFactory() {
        return this.randomNumberFactory;
    }

    /**
     * @return the rules
     */
    @Override
    public RuleHolder getRules() {
        return this.rules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdjustScore> getScoreAdjusters() {
        return this.adjusters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CalculateScore getScoreFunction() {
        return this.scoreFunction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SelectionOperator getSelection() {
        return this.selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenomeComparator getSelectionComparator() {
        return this.selectionComparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getShouldIgnoreExceptions() {
        return this.ignoreExceptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Speciation getSpeciation() {
        return this.speciation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getThreadCount() {
        return this.threadCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidationMode() {
        return this.validationMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iteration() {

        if (this.actualThreadCount == -1) {
            preIteration();
        }

        if (population.getSpecies().size() == 0) {
            throw new EncogError("Population is empty, there are no species.");
        }

        this.iteration++;

        // make new teams and evaluate using the TeamEvaluator.
        //Tricky part is how to keep team sizes constant while having unique genomes per team member and
        //evaluating all genomes in candidate pool.

        //Create all children first. then evaluate them. then put them through the workers and operators.

        // Clear new population to just best genome.
        this.newPopulation.clear();
        this.teamPopulation.clear();

        this.newPopulation.add(this.bestGenome);
        this.teamPopulation.add(this.bestGenome);

        this.oldBestGenome = this.bestGenome;

        // execute species in parallel
        this.threadList.clear();

        for (final Species species : population.getSpecies()) {
            int numToSpawn = species.getOffspringCount();

            if (species.getMembers().size() > 5) {
                final int idealEliteCount = (int) (species.getMembers().size() * getEliteRate());
                final int eliteCount = Math.min(numToSpawn, idealEliteCount);
                for (int i = 0; i < eliteCount; i++) {
                    final Genome eliteGenome = species.getMembers().get(i);
                    if (getOldBestGenome() != eliteGenome) {
                        numToSpawn--;
                        if (!addChildToTemp(eliteGenome)) {
                            break;
                        }
                    }
                }
            }

            // now apply evolutionary operators to the last generation's pool, and add it to the temp population
            while (numToSpawn-- > 0) {
                final CCHEABreeder worker = new CCHEABreeder(this, species);
                this.threadList.add(worker);
            }
        }

        // run all threads and wait for them to finish
        try {
            this.taskExecutor.invokeAll(this.threadList);
        } catch (final InterruptedException e) {
            EncogLogging.log(e);
        }

        //now create the teams and evaluate them in their separate threads,
        // keeping their score within their CCHIndividual wrappers.
        NEATTeamFactory teamFactory = new NEATTeamFactory(experimentConfig, teamPopulation);
        //for however number of teamruns per pool
        List<NEATTeam> totalTeams = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < experimentConfig.getRunsPerGenome(); i++) {

            //now create the teams and evaluate them in their separate threads,
            // keeping their score within their CCHIndividual wrappers.
            List<NEATTeam> teams = teamFactory.placeInTeams();
            totalTeams.addAll(teams);

            for (int j = 0; j < teams.size(); j++) {

                executor.execute(new TeamEvaluator(experimentConfig, simConfig, morphologyConfig, teams.get(j)));
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("sh*t.");
        }

        Collections.sort(totalTeams);
        currentBestTeam = totalTeams.get(totalTeams.size()-1);
        bestTeam = totalTeams.get(totalTeams.size()-1).compareTo(bestTeam) > 0 ? totalTeams.get(totalTeams.size()-1) : bestTeam;

        // score the population
        final CCHParallelScore pscore = new CCHParallelScore(population, teamFactory.getAllIndividuals(),
                new ArrayList<AdjustScore>(), getScoreFunction(),
                this.actualThreadCount);
        pscore.process();

        //NaNs and shit should be at the beginning i hope (idk for positive infinity)
        List<CCHIndividual> flattenedIndividuals = new ArrayList<>(teamFactory.getAllIndividuals());
        Collections.sort(flattenedIndividuals);
        currentBestIndividual = flattenedIndividuals.get(flattenedIndividuals.size()-1);
        double[] doubleArray = new double[flattenedIndividuals.size()];

        int z = 0;
        for (CCHIndividual individual: flattenedIndividuals) {
            doubleArray[z] = individual.getGenome().getScore();
            z++;
        }

        lastEpochScores = thisEpochScores;
        thisEpochScores = doubleArray;

        if (flattenedIndividuals.get(flattenedIndividuals.size()-1).compareTo(bestIndividual) > 0) {
            bestIndividual = flattenedIndividuals.get(flattenedIndividuals.size() - 1);
        }

        // just pick the first genome with a valid score as best, it will be
        // updated later.
        // also most populations are sorted this way after training finishes
        // (for reload)
        // if there is an empty population, the constructor would have blow
        final List<Genome> list = population.flatten();

        int idx = 0;
        do {
            this.bestGenome = list.get(idx++);
        } while (idx < list.size()
                && (Double.isInfinite(this.bestGenome.getScore()) || Double
                .isNaN(this.bestGenome.getScore())));

        population.setBestGenome(this.bestGenome);

        newPopulation.clear();
        newPopulation.addAll(teamPopulation);

        // handle any errors that might have happened in the threads
        if (this.reportedError != null && !getShouldIgnoreExceptions()) {
            throw new GeneticError(this.reportedError);
        }

        // validate, if requested
        if (isValidationMode()) {
            if (this.oldBestGenome != null
                    && !this.newPopulation.contains(this.oldBestGenome)) {
                throw new EncogError(
                        "The top genome died, this should never happen!!");
            }

            if (this.bestGenome != null
                    && this.oldBestGenome != null
                    && getBestComparator().isBetterThan(this.oldBestGenome,
                    this.bestGenome)) {
                throw new EncogError(
                        "The best genome's score got worse, this should never happen!! Went from "
                                + this.oldBestGenome.getScore() + " to "
                                + this.bestGenome.getScore());
            }
        }

        this.speciation.performSpeciation(this.newPopulation);

        // purge invalid genomes
        this.population.purgeInvalidGenomes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performShutdownTask() {
        finishTraining();
    }

    /**
     * Called before the first iteration. Determine the number of threads to
     * use.
     */
    private void preIteration() {

        this.speciation.init(this);

        // find out how many threads to use
        if (this.threadCount == 0) {
            this.actualThreadCount = Runtime.getRuntime().availableProcessors();
        } else {
            this.actualThreadCount = this.threadCount;
        }

        //for however number of teamruns per pool

        NEATTeamFactory teamFactory = new NEATTeamFactory(experimentConfig, population.flatten());
        List<NEATTeam> totalTeams = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < experimentConfig.getRunsPerGenome(); i++) {

            //now create the teams and evaluate them in their separate threads,
            // keeping their score within their CCHIndividual wrappers.
            List<NEATTeam> teams = teamFactory.placeInTeams();
            totalTeams.addAll(teams);

            for (int j = 0; j < teams.size(); j++) {

                executor.execute(new TeamEvaluator(experimentConfig, simConfig, morphologyConfig, teams.get(j)));
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("sh*t.");
        }

        while (!executor.isTerminated()) {
        }

        Collections.sort(totalTeams);
        bestTeam = totalTeams.get(totalTeams.size()-1);
        currentBestTeam = bestTeam;

        // score the initial population
        final CCHParallelScore pscore = new CCHParallelScore(population, teamFactory.getAllIndividuals(),
                 new ArrayList<AdjustScore>(), getScoreFunction(),
                this.actualThreadCount);

        pscore.setThreadCount(this.actualThreadCount);
        pscore.process();
        this.actualThreadCount = pscore.getThreadCount();

        // start up the thread pool
        if (this.actualThreadCount == 1) {
            this.taskExecutor = Executors.newSingleThreadScheduledExecutor();
        } else {
            this.taskExecutor = Executors
                    .newFixedThreadPool(this.actualThreadCount);
        }

        // register for shutdown
        Encog.getInstance().addShutdownTask(this);

        //NaNs and shit should be at the beginning i hope (idk for positive infinity)
        List<CCHIndividual> flattenedIndividuals = new ArrayList<>(teamFactory.getAllIndividuals());
        Collections.sort(flattenedIndividuals);

        bestIndividual = flattenedIndividuals.get(flattenedIndividuals.size()-1);
        currentBestIndividual = bestIndividual;

        lastEpochScores = new double[flattenedIndividuals.size()];
        thisEpochScores = new double[flattenedIndividuals.size()];

        int z = 0;
        for (CCHIndividual individual: flattenedIndividuals) {
            thisEpochScores[z] = individual.getGenome().getScore();
            z++;
        }
        lastEpochScores = thisEpochScores;

        // just pick the first genome with a valid score as best, it will be
        // updated later.
        // also most populations are sorted this way after training finishes
        // (for reload)
        // if there is an empty population, the constructor would have blow
        final List<Genome> list = population.flatten();

        int idx = 0;
        do {
            this.bestGenome = list.get(idx++);
        } while (idx < list.size()
                && (Double.isInfinite(this.bestGenome.getScore()) || Double
                .isNaN(this.bestGenome.getScore())));

        population.setBestGenome(this.bestGenome);

        // speciate
        final List<Genome> genomes = teamFactory.getGenomePopulation();
        this.speciation.performSpeciation(genomes);

        // purge invalid genomes
        this.population.purgeInvalidGenomes();
    }

    /**
     * Called by a thread to report an error.
     *
     * @param t
     *            The error reported.
     */
    public void reportError(final Throwable t) {
        synchronized (this) {
            if (this.reportedError == null) {
                this.reportedError = t;
            }
        }
    }

    /**
     * Set the comparator.
     *
     * @param theComparator
     *            The comparator.
     */
    @Override
    public void setBestComparator(final GenomeComparator theComparator) {
        this.bestComparator = theComparator;
    }

    /**
     * @param champMutation
     *            the champMutation to set
     */
    public void setChampMutation(final EvolutionaryOperator champMutation) {
        this.champMutation = champMutation;
    }

    /**
     * Set the CODEC to use.
     *
     * @param theCodec
     *            The CODEC to use.
     */
    public void setCODEC(final GeneticCODEC theCodec) {
        this.codec = theCodec;
    }

    /**
     * @param eliteRate
     *            the eliteRate to set
     */
    public void setEliteRate(final double eliteRate) {
        this.eliteRate = eliteRate;
    }

    /**
     * Set the current iteration number.
     *
     * @param iteration
     *            The iteration number.
     */
    public void setIteration(final int iteration) {
        this.iteration = iteration;
    }

    /**
     * @param maxTries
     *            the maxTries to set
     */
    public void setMaxTries(final int maxTries) {
        this.maxTries = maxTries;
    }

    /**
     * {@inheritDoc}
     */

    public void setPopulation(final CCHNEATPopulation thePopulation) {
        this.population = thePopulation;
    }

    /**
     * @param randomNumberFactory
     *            the randomNumberFactory to set
     */
    public void setRandomNumberFactory(final RandomFactory randomNumberFactory) {
        this.randomNumberFactory = randomNumberFactory;
    }

    /**
     * @param rules
     *            the rules to set
     */
    @Override
    public void setRules(final RuleHolder rules) {
        this.rules = rules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(final SelectionOperator selection) {
        this.selection = selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectionComparator(final GenomeComparator theComparator) {
        this.selectionComparator = theComparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShouldIgnoreExceptions(final boolean b) {
        this.ignoreExceptions = b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpeciation(final Speciation speciation) {
        this.speciation = speciation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setThreadCount(final int numThreads) {
        this.threadCount = numThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValidationMode(final boolean validationMode) {
        this.validationMode = validationMode;
    }

    /**
     * @return the maxOperationErrors
     */
    public int getMaxOperationErrors() {
        return maxOperationErrors;
    }

    /**
     * @param maxOperationErrors the maxOperationErrors to set
     */
    public void setMaxOperationErrors(int maxOperationErrors) {
        this.maxOperationErrors = maxOperationErrors;
    }


    public List<Genome> getTeamPopulation() { return teamPopulation; }

    public double mannWhitneyImprovementTest() {

        MannWhitneyUTest mwTest = new MannWhitneyUTest();

        if (lastEpochScores == null) {
            lastEpochScores = new double[thisEpochScores.length];
        }

        return mwTest.mannWhitneyU(thisEpochScores, lastEpochScores);
    }

    public NEATTeam getBestTeam() { return bestTeam; }

    public NEATTeam getCurrentBestTeam() { return currentBestTeam; }

    public double getVariance() {

        Variance variance = new Variance();
        double raw = variance.evaluate(thisEpochScores);
        return raw;
    }

    public double getStandardDeviation() {

        StandardDeviation stdDev = new StandardDeviation();
        return stdDev.evaluate(thisEpochScores);
    }

    public double getEpochMean() {

        Mean mean = new Mean();
        return mean.evaluate(thisEpochScores);
    }

    //get best individual so far
    public CCHIndividual getBestIndividual() { return bestIndividual; }

    public CCHIndividual getCurrentBestIndividual() { return currentBestIndividual; }

}

