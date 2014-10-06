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


        Simulation simulation = new Simulation(simConfig, heteroFactory, team.getGenotypes());
        simulation.run();

    }


    private class SimRun implements Runnable {

        private final Simulation simulation;
        private final double[] scores;
        private int ticketNo;


        public SimRun(Simulation simulation, double[] scores, int ticketNo) {

            this.simulation = simulation;
            this.scores = scores;
            this.ticketNo = ticketNo;
        }

        public void run() {

            simulation.run();
            scores[ticketNo] = simulation.getFitness();
        }
    }

}
