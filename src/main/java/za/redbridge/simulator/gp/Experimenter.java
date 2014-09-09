package za.redbridge.simulator.gp;

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

        ResourceFactory resourceFactory = new ConfigurableResourceFactory(config.getSmallObjectWidth(), config.getSmallObjectHeight(),
                config.getSmallObjectMass(), config.getSmallObjectPushingBots(), config.getLargeObjectWidth(), config.getLargeObjectHeight(),
                config.getLargeObjectMass(), config.getLargeObjectPushingBots());

        RobotFactory robotFactory = new HomogeneousRobotFactory(new ChasingPhenotype(), 0.7, 0.15,
                new Color(0,0,0));
        List<AgentSensor> sensors = new ArrayList<AgentSensor>();
        AgentSensor leftSensor = new ProximityAgentSensor((float) ((7 / 4.0f) * Math.PI), 0f, 1f, 0.2f);
        AgentSensor forwardSensor = new ProximityAgentSensor(0f, 0f, 1f, 0.2f);
        AgentSensor rightSensor = new ProximityAgentSensor((float) (Math.PI/4), 0f, 1f, 0.2f);

        sensors.add(leftSensor);
        sensors.add(forwardSensor);
        sensors.add(rightSensor);

        //TODO refactor to pass in the factories; we need to use those in getFitness()
        AgentModel model = new AgentModel(sensors);
        model.run();
        Simulation sim = new Simulation(robotFactory, resourceFactory, config);
    }
}
