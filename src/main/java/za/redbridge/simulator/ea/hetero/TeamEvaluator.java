package za.redbridge.simulator.ea.hetero;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import za.redbridge.simulator.FitnessStats;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HeteroTeamRobotFactory;
import za.redbridge.simulator.factories.TeamPhenotypeFactory;
import za.redbridge.simulator.phenotype.Phenotype;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by racter on 2014/10/05.
 * Evaluates one team in the context of a simulation.
 */
public class TeamEvaluator implements Runnable {


    private SimConfig simConfig;
    private MorphologyConfig morphologyConfig;
    private ExperimentConfig experimentConfig;
    private NEATTeam team;
    private final double[] scores;

    public TeamEvaluator(ExperimentConfig experimentConfig, SimConfig simConfig, MorphologyConfig morphologyConfig, NEATTeam team) {

        this.simConfig = simConfig;
        this.experimentConfig = experimentConfig;
        this.morphologyConfig = morphologyConfig;
        this.team = team;
        scores = new double[experimentConfig.getRunsPerTeam()];
    }

    public void run() {


        EvaluateTeams[] evaluateTeams = new EvaluateTeams[experimentConfig.getRunsPerTeam()];

        for (int i = 0; i < evaluateTeams.length; i++) {

            evaluateTeams[i] = new EvaluateTeams(simConfig, experimentConfig, morphologyConfig, team, scores, i);
            evaluateTeams[i].run();
        }

        Mean mean = new Mean();
        team.setTeamFitness(mean.evaluate(scores));
    }

    private static class EvaluateTeams implements Runnable {

        private final SimConfig simConfig;
        private final ExperimentConfig experimentConfig;
        private final MorphologyConfig morphologyConfig;
        private final NEATTeam team;
        private final double[] scores;
        private final int ticketNo;

        public EvaluateTeams (final SimConfig simConfig,
                              final ExperimentConfig experimentConfig,
                              final MorphologyConfig morphologyConfig,
                              final NEATTeam team,
                              final double[] scores,
                              final int ticketNo) {

            this.simConfig = simConfig;
            this.experimentConfig = experimentConfig;
            this.morphologyConfig = morphologyConfig;
            this.team = team;
            this.scores = scores;
            this.ticketNo = ticketNo;
        }

        public void run() {

            TeamPhenotypeFactory phenotypeFactory = new TeamPhenotypeFactory(morphologyConfig, team.getGenotypes());

            HeteroTeamRobotFactory heteroFactory = new HeteroTeamRobotFactory(phenotypeFactory.generatePhenotypeTeam(),
                    simConfig.getRobotMass(), simConfig.getRobotRadius(), simConfig.getRobotColour());

            Simulation simulation = new Simulation(simConfig, heteroFactory);
            simulation.run();

            FitnessStats fitnessStats = simulation.getFitness();
            Map<Phenotype,Double> fitnesses = fitnessStats.getPhenotypeFitnessMap();

            for (Map.Entry<Phenotype,Double> entry: fitnesses.entrySet()) {

                entry.getKey().getController().addTaskScore(entry.getValue());
            }

            scores[ticketNo] = fitnessStats.getTeamFitness(Optional.of(simulation.getStepNumber()));
        }
    }
}
