package za.redbridge.simulator.experiment;

import org.epochx.epox.Node;
import org.epochx.gp.op.init.RampedHalfAndHalfInitialiser;
import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.life.GenerationListener;
import org.epochx.life.Life;
import org.epochx.op.selection.FitnessProportionateSelector;
import org.epochx.op.selection.TournamentSelector;
import org.epochx.stats.StatField;
import org.epochx.stats.Stats;
import org.epochx.tools.eval.MalformedProgramException;
import org.jbox2d.dynamics.Fixture;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sim.display.Console;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.gp.AgentModel;
import za.redbridge.simulator.khepera.BottomProximitySensor;
import za.redbridge.simulator.khepera.KheperaIIIPhenotype;
import za.redbridge.simulator.khepera.ProximitySensor;
import za.redbridge.simulator.khepera.UltrasonicSensor;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.phenotype.GPPhenotype;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ProximityAgentSensor;

//entry point into simulator

/**
 * Created by racter on 2014/08/19.
 */
public class Main {

    //config files for this experiment

    @Option(name="--experiment-config", usage="Filename for experiment configuration", metaVar="<experiment config>")
    private String experimentConfig;

    @Option (name="--simulation-config", usage="Filename for simulation configuration", metaVar="<simulation config>")
    private String simulationConfig;

    @Option (name="--show-visuals", aliases="-v", usage="Show visualisation for simulation")
    private boolean showVisuals = false;

    public static void main (String[] args) throws MalformedProgramException{

        Main options = new Main();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException c) {
            System.out.println("Error parsing command-line arguments.");
            System.exit(1);
        }

        ExperimentConfig experimentConfiguration = new ExperimentConfig(options.getExperimentConfig());
        SimConfig simulationConfiguration = new SimConfig(options.getSimulationConfig());

        //TODO: work with multiple morphology configs (specifically, filter sensitivities)
        MorphologyConfig morphologyConfig = null;

        try {
            morphologyConfig = new MorphologyConfig(experimentConfiguration.getMorphologyConfigFile());
        }
        catch(ParseException p) {
            System.out.println("Error parsing morphology file.");
            p.printStackTrace();
        }

        List<AgentSensor> sensors = new ArrayList<>();
        sensors.add(new UltrasonicSensor(0f, 0f));
        sensors.add(new UltrasonicSensor((float)Math.PI/2, 0f));
        sensors.add(new UltrasonicSensor((float)Math.PI, 0f));
        sensors.add(new UltrasonicSensor((float)(3*Math.PI/2), 0f));

        sensors.add(new BottomProximitySensor());

        AgentModel model = new AgentModel(sensors, simulationConfiguration, experimentConfiguration);
        model.setNoGenerations(100);
        model.setMaxInitialDepth(5);
        model.setMaxDepth(7);
        model.setPopulationSize(200);
        model.setPoolSize(model.getPopulationSize() / 2);
        model.setProgramSelector(new TournamentSelector(model, 10));
        model.setNoRuns(1);
        model.setNoElites(model.getPopulationSize() / 10);
        model.setInitialiser(new RampedHalfAndHalfInitialiser(model));
        model.setMutationProbability(0.1);
        model.setCrossoverProbability(0.9);
        model.setTerminationFitness(Double.NEGATIVE_INFINITY);
        class GenerationTrackingListener implements GenerationListener{
            private int counter = 0;
            private Long startTime = null;
            @Override
            public void onGenerationEnd() {
                if(startTime == null) startTime = System.currentTimeMillis();
                Stats s = Stats.get();
                System.out.println();
                System.out.println("Generation " + (counter+1));
                Double min = (Double)s.getStat(StatField.GEN_FITNESS_MIN);
                Double avg = (Double)s.getStat(StatField.GEN_FITNESS_AVE);
                System.out.println(); //newline after the dots
                System.out.println("Fitness: " + min);
                System.out.println("Avg: " + avg);
                s.print(StatField.GEN_FITTEST_PROGRAM);
                Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - startTime);
                System.out.println("Elapsed: " + elapsed.toString());
                counter++;
            }
            @Override
            public void onGenerationStart(){}
        }
        Life.get().addGenerationListener(new GenerationTrackingListener());

        //if we need to show a visualisation
        if (options.showVisuals()) {
            String tree = "WHEELDRIVEFROMBEARING(IF(READINGPRESENT(IF(IF(TS4 TS4 TS4) IF(TS4 PS2 PS3) IF(TS4 PS2 PS3))) BEARINGFROMCOORDINATE(IF(READINGPRESENT(PS0) READINGTOCOORDINATE(PS0) READINGTOCOORDINATE(PS0))) BEARINGFROMCOORDINATE(ROTATECOORDINATE(READINGTOCOORDINATE(IF(TS4 IF(TS4 PS2 PS2) PS0)) BEARINGFROMCOORDINATE(ROTATECOORDINATE(READINGTOCOORDINATE(PS1) SAVEBEARING(B4.71)))))))";
            Node root = model.getParser().parse(tree);
            GPCandidateProgram cand = new GPCandidateProgram(root, model);
            HomogeneousRobotFactory robotFactory = new HomogeneousRobotFactory(
                    new GPPhenotype(sensors, cand, model.getInputs()), simulationConfiguration.getRobotMass(),
                    simulationConfiguration.getRobotRadius(), simulationConfiguration.getRobotColour(), simulationConfiguration.getObjectsRobots());

            Simulation simulation = new Simulation(simulationConfiguration, robotFactory);
            SimulationGUI video = new SimulationGUI(simulation);

            //new console which displays this simulation
            Console console = new Console(video);
            console.setVisible(true);
        }
        else {
            System.out.println("Commencing experiment");
            //headless option
            model.run();
            System.out.println("Experiment finished. Best fitness: ");
            Stats s = Stats.get();
            s.print(StatField.ELITE_FITNESS_MIN);
            System.out.println("Best programs: ");
            s.print(StatField.GEN_FITTEST_PROGRAMS);
        }

    }

    public String getExperimentConfig() { return experimentConfig; }
    public String getSimulationConfig() { return simulationConfig; }
    public boolean showVisuals() { return showVisuals; }


}
