package za.redbridge.simulator.ea.hetero;

import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HeteroTeamRobotFactory;
import za.redbridge.simulator.factories.TeamPhenotypeFactory;


/**
 * Created by racter on 2014/10/05.
 * Evaluates team in the context of a simulation.
 */
public class TeamEvaluator implements Runnable {


    private SimConfig simConfig;
    private MorphologyConfig morphologyConfig;
    private ExperimentConfig experimentConfig;
    private Team team;

    public TeamEvaluator(ExperimentConfig experimentConfig, SimConfig simConfig, MorphologyConfig morphologyConfig, Team team) {

        this.simConfig = simConfig;
        this.experimentConfig = experimentConfig;
        this.morphologyConfig = morphologyConfig;
        this.team = team;
    }

    public void run() {

        //This does not work because TeamPhenotypeFactory is an unimplemented interface

        /*
        TeamPhenotypeFactory phenotypeFactory = new TeamPhenotypeFactory(morphologyConfig, team.getGenomes());

        HeteroTeamRobotFactory heteroFactory = new HeteroTeamRobotFactory(phenotypeFactory.generatePhenotypeTeam(),
                 simConfig.getRobotMass(), simConfig.getRobotRadius(), simConfig.getRobotColour());


        Simulation simulation = new Simulation(simConfig, heteroFactory, team.getScoreKeepingGenotypes());
        simulation.run();*/

    }
}
