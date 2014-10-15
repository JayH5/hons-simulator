package za.redbridge.simulator.ea.hetero;

import za.redbridge.simulator.FitnessStats;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HeteroTeamRobotFactory;
import za.redbridge.simulator.factories.TeamPhenotypeFactory;
import za.redbridge.simulator.phenotype.Phenotype;

import java.util.Map;


/**
 * Created by racter on 2014/10/05.
 * Evaluates one team in the context of a simulation.
 */
public class TeamEvaluator implements Runnable {


    private SimConfig simConfig;
    private MorphologyConfig morphologyConfig;
    private ExperimentConfig experimentConfig;
    private NEATTeam team;

    public TeamEvaluator(ExperimentConfig experimentConfig, SimConfig simConfig, MorphologyConfig morphologyConfig, NEATTeam team) {

        this.simConfig = simConfig;
        this.experimentConfig = experimentConfig;
        this.morphologyConfig = morphologyConfig;
        this.team = team;
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

        team.setTeamFitness(fitnessStats.getTeamFitness());
    }
}
