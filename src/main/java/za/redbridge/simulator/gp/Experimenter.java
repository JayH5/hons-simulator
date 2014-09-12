package za.redbridge.simulator.gp;

import org.epochx.gp.op.init.GrowInitialiser;
import org.epochx.gp.op.init.RampedHalfAndHalfInitialiser;
import org.epochx.life.GenerationAdapter;
import org.epochx.life.GenerationListener;
import org.epochx.life.Life;
import org.epochx.life.RunAdapter;
import org.epochx.stats.StatField;
import org.epochx.stats.Stats;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.ConfigurableResourceFactory;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.factories.ResourceFactory;
import za.redbridge.simulator.factories.RobotFactory;
import za.redbridge.simulator.phenotype.ChasingPhenotype;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ProximityAgentSensor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xenos on 9/9/14.
 */
public class Experimenter {
    public static void main(String[] args){
        List<AgentSensor> sensors = new ArrayList<>();
        AgentSensor leftSensor = new ProximityAgentSensor((float) ((7 / 4.0f) * Math.PI), 0f, 1f, 0.2f);
        AgentSensor forwardSensor = new ProximityAgentSensor(0f, 0f, 1f, 0.2f);
        AgentSensor rightSensor = new ProximityAgentSensor((float) (Math.PI/4), 0f, 1f, 0.2f);

        sensors.add(leftSensor);
        sensors.add(forwardSensor);
        sensors.add(rightSensor);

        SimConfig simulationConfiguration = new SimConfig("configs/simulationConfig.yml");
        ExperimentConfig experimentConfiguration = new ExperimentConfig("configs/experimentConfig.yml");

        AgentModel model = new AgentModel(sensors, simulationConfiguration, experimentConfiguration);
        model.setNoGenerations(100);
        model.setMaxInitialDepth(6);
        model.setMaxDepth(7);
        model.setPoolSize(100);
        model.setPopulationSize(200);
        model.setNoRuns(1);
        model.setInitialiser(new RampedHalfAndHalfInitialiser(model));
        model.setTerminationFitness(Double.NEGATIVE_INFINITY);
        Life.get().addGenerationListener(new GenerationAdapter() {
            public void onGenerationEnd() {
                Stats s = Stats.get();
                System.out.println();
                s.print(StatField.ELITE_FITNESS_MIN);
                s.print(StatField.GEN_FITTEST_PROGRAMS);
            }
        });
        model.run();
        System.out.println("Experiment finished. Best fitness: ");
        Stats s = Stats.get();
        s.print(StatField.ELITE_FITNESS_MIN);
        System.out.println("Best programs: ");
        s.print(StatField.GEN_FITTEST_PROGRAMS);
    }
}
