package za.redbridge.simulator.gp;

import org.epochx.life.Life;
import org.epochx.stats.StatField;
import org.epochx.stats.Stats;
import za.redbridge.simulator.Simulation;
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
        SimConfig config;
        if (args.length > 0) {
            config = SimConfig.loadFromFile(args[0]);
        } else {
            config = new SimConfig(); // Default
        }

        List<AgentSensor> sensors = new ArrayList<AgentSensor>();
        AgentSensor leftSensor = new ProximityAgentSensor((float) ((7 / 4.0f) * Math.PI), 0f, 1f, 0.2f);
        AgentSensor forwardSensor = new ProximityAgentSensor(0f, 0f, 1f, 0.2f);
        AgentSensor rightSensor = new ProximityAgentSensor((float) (Math.PI/4), 0f, 1f, 0.2f);

        sensors.add(leftSensor);
        sensors.add(forwardSensor);
        sensors.add(rightSensor);

        //TODO refactor to pass in the factories; we need to use those in getFitness()
        AgentModel model = new AgentModel(sensors, config);
        model.setNoGenerations(10000);
        model.setTerminationFitness(Double.NEGATIVE_INFINITY);
        model.run();
        Stats s = Stats.get();
        System.out.println("Yay");
        s.print(StatField.ELITE_FITNESS_MIN);
    }
}
