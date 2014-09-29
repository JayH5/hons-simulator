package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.phenotype.NEATPhenotype;

import java.text.ParseException;

/**
 * Created by shsu on 2014/09/22.
 */
public class SimulationVisual {

    private final NEATNetwork network;
    private final MorphologyConfig morphologyConfig;
    private final SimConfig simulationConfiguration;

    public SimulationVisual(SimConfig simulationConfiguration, String controllerFile, String morphologyFile) {

        String[] splitPath = controllerFile.split("/");
        String filename = splitPath[splitPath.length-2];

        network = IOUtils.readNetwork(controllerFile);
        morphologyConfig = new MorphologyConfig(morphologyFile);
        this.simulationConfiguration = simulationConfiguration;
    }

    public void run() {

        HomogeneousRobotFactory robotFactory = new HomogeneousRobotFactory(
                new NEATPhenotype(morphologyConfig.getSensorList(), network, morphologyConfig.getTotalReadingSize()),
                simulationConfiguration.getRobotMass(),
                simulationConfiguration.getRobotRadius(), simulationConfiguration.getRobotColour(),
                simulationConfiguration.getObjectsRobots());

        Simulation simulation = new Simulation(simulationConfiguration, robotFactory);

        SimulationGUI video = new SimulationGUI(simulation);

        //new console which displays this simulation
        sim.display.Console console = new sim.display.Console(video);
        console.setVisible(true);
    }
}
