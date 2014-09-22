package za.redbridge.simulator.experiment;

import org.epochx.epox.Node;
import org.epochx.gp.op.init.RampedHalfAndHalfInitialiser;
import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.life.GenerationListener;
import org.epochx.life.Life;
import org.epochx.op.selection.FitnessProportionateSelector;
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

import sim.display.Console;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.gp.AgentModel;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.phenotype.GPPhenotype;
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
        AgentSensor leftSensor = new ResourceProximitySensor((float) ((7 / 4.0f) * Math.PI), 0f, 1f, 0.2f);
        AgentSensor forwardSensor = new ResourceProximitySensor(0f, 0f, 1f, 0.2f);
        AgentSensor rightSensor = new ResourceProximitySensor((float) (Math.PI/4), 0f, 1f, 0.2f);

        sensors.add(leftSensor);
        sensors.add(forwardSensor);
        sensors.add(rightSensor);

        AgentModel model = new AgentModel(sensors, simulationConfiguration, experimentConfiguration);
        model.setNoGenerations(100);
        model.setMaxInitialDepth(6);
        model.setMaxDepth(7);
        model.setPopulationSize(200);
        model.setPoolSize(model.getPopulationSize()/2);
        model.setProgramSelector(new FitnessProportionateSelector(model));
        model.setNoRuns(1);
        model.setNoElites(0);
        model.setInitialiser(new RampedHalfAndHalfInitialiser(model));
        model.setTerminationFitness(Double.NEGATIVE_INFINITY);
        class GenerationTrackingListener implements GenerationListener{
            private int counter = 0;
            private Long startTime = null;
            @Override
            public void onGenerationEnd() {
                if(startTime == null) startTime = System.currentTimeMillis();
                Stats s = Stats.get();
                System.out.println();
                Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - startTime);
                s.print(StatField.GEN_FITNESS_MIN);
                s.print(StatField.GEN_FITTEST_PROGRAM);
                System.out.println("Generation " + (counter+1) + ", " + elapsed.toString());
                counter++;
            }
            @Override
            public void onGenerationStart(){}
        }
        Life.get().addGenerationListener(new GenerationTrackingListener());

        //if we need to show a visualisation
        if (options.showVisuals()) {
            String oldBuggy = "IF(GT(IF(GT(1.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S0)) IF(GT(0.0 0.0) READINGTODISTANCE(S1) READINGTODISTANCE(S1))) IF(IF(GT(0.0 1.0) GT(0.0 1.0) GT(IF(GT(0.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) 1.0)) IF(GT(1.0 1.0) WHEELDRIVEFROMFLOATS(IF(GT(0.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) 1.0) WHEELDRIVEFROMFLOATS(1.0 0.0)) WHEELDRIVEFROMFLOATS(READINGTODISTANCE(S1) 1.0)) WHEELDRIVEFROMFLOATS(IF(GT(0.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) IF(GT(0.0 0.0) READINGTODISTANCE(S1) READINGTODISTANCE(S0))))";
            String newMaybeFixed = "WHEELDRIVEFROMFLOATS(IF(GT(READINGTODISTANCE(S1) READINGTODISTANCE(S0)) IF(GT(0.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S1)) IF(GT(1.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S1))) IF(IF(GT(1.0 0.0) GT(0.0 0.0) GT(1.0 0.0)) IF(GT(1.0 0.0) READINGTODISTANCE(S0) READINGTODISTANCE(S2)) IF(GT(0.0 0.0) READINGTODISTANCE(S0) READINGTODISTANCE(S0))))";
            String badass = "WHEELDRIVEFROMFLOATS(IF(GT(READINGTODISTANCE(S2) 0.0) IF(IF(GT(1.0 1.0) GT(1.0 1.0) GT(0.0 1.0)) IF(GT(1.0 0.0) READINGTODISTANCE(S0) READINGTODISTANCE(S1)) IF(GT(1.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S1))) IF(IF(GT(1.0 1.0) GT(0.0 1.0) GT(0.0 0.0)) IF(GT(1.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) IF(GT(1.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S0)))) IF(IF(GT(READINGTODISTANCE(S2) READINGTODISTANCE(S0)) GT(1.0 0.0) GT(READINGTODISTANCE(S0) READINGTODISTANCE(S2))) IF(IF(GT(1.0 1.0) GT(1.0 1.0) GT(0.0 0.0)) IF(GT(1.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S0)) IF(GT(0.0 1.0) READINGTODISTANCE(S1) 0.0)) IF(GT(READINGTODISTANCE(S1) READINGTODISTANCE(S2)) IF(GT(READINGTODISTANCE(S0) 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) IF(GT(0.0 0.0) READINGTODISTANCE(S0) READINGTODISTANCE(S0)))))";
            String rofl = "IF(GT(1.0 1.0) WHEELDRIVEFROMCOORD(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00)) WHEELDRIVEFROMCOORD(COORDINATEFROMDISTANCEANDBEARING(0.0 B3.14)))";
            String difficult = "IF(GT(IF(GT(IF(GT(0.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S0)) IF(GT(0.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S2))) IF(IF(GT(1.0 1.0) GT(0.0 0.0) GT(1.0 0.0)) IF(GT(1.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) IF(GT(1.0 0.0) READINGTODISTANCE(S1) READINGTODISTANCE(S0))) IF(GT(READINGTODISTANCE(S0) READINGTODISTANCE(S2)) IF(GT(0.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S1)) IF(GT(0.0 0.0) READINGTODISTANCE(S1) READINGTODISTANCE(S2)))) IF(IF(IF(GT(0.0 0.0) GT(0.0 0.0) GT(1.0 1.0)) GT(READINGTODISTANCE(S0) READINGTODISTANCE(S1)) GT(READINGTODISTANCE(S2) READINGTODISTANCE(S0))) IF(IF(GT(0.0 1.0) GT(0.0 1.0) GT(1.0 1.0)) IF(GT(1.0 1.0) READINGTODISTANCE(S0) READINGTODISTANCE(S2)) IF(GT(1.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S0))) IF(IF(GT(1.0 0.0) GT(1.0 1.0) GT(1.0 1.0)) IF(GT(1.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) IF(GT(0.0 0.0) READINGTODISTANCE(S1) READINGTODISTANCE(S0))))) WHEELDRIVEFROMFLOATS(IF(IF(IF(GT(1.0 0.0) GT(1.0 0.0) GT(0.0 0.0)) IF(GT(0.0 1.0) GT(0.0 1.0) GT(1.0 0.0)) GT(READINGTODISTANCE(S0) READINGTODISTANCE(S0))) 1.0 IF(IF(GT(0.0 1.0) GT(1.0 0.0) GT(1.0 1.0)) IF(GT(1.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S0)) IF(GT(1.0 1.0) READINGTODISTANCE(S1) 1.0))) IF(IF(IF(GT(0.0 1.0) GT(0.0 0.0) GT(0.0 0.0)) IF(GT(0.0 1.0) GT(1.0 0.0) GT(0.0 0.0)) GT(READINGTODISTANCE(S0) READINGTODISTANCE(S1))) IF(GT(READINGTODISTANCE(S0) READINGTODISTANCE(S2)) IF(GT(0.0 0.0) READINGTODISTANCE(S0) READINGTODISTANCE(S1)) IF(GT(1.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S1))) IF(IF(GT(0.0 1.0) GT(0.0 1.0) GT(1.0 1.0)) IF(GT(0.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S0)) IF(GT(0.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S1))))) WHEELDRIVEFROMCOORD(IF(GT(IF(GT(0.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S2)) IF(GT(1.0 0.0) READINGTODISTANCE(S1) READINGTODISTANCE(S2))) ROTATECOORDINATE(IF(GT(1.0 1.0) COORDINATEFROMDISTANCEANDBEARING(1.0 B4.71) COORDINATEFROMDISTANCEANDBEARING(1.0 B.00)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00))) ROTATECOORDINATE(IF(GT(0.0 0.0) COORDINATEFROMDISTANCEANDBEARING(0.0 B.00) COORDINATEFROMDISTANCEANDBEARING(0.0 B.00)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(0.0 B3.14))))))";
            //--shit be fixed now
            String fixed = "WHEELDRIVEFROMCOORD(COORDINATEFROMDISTANCEANDBEARING(IF(GT(IF(GT(0.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) IF(GT(0.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2))) IF(IF(GT(1.0 1.0) GT(1.0 0.0) GT(1.0 0.0)) IF(GT(1.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S1)) IF(GT(0.0 0.0) READINGTODISTANCE(S0) READINGTODISTANCE(S1))) IF(IF(GT(0.0 1.0) GT(0.0 0.0) GT(0.0 1.0)) IF(GT(1.0 0.0) READINGTODISTANCE(S1) READINGTODISTANCE(S1)) IF(GT(1.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S0)))) IF(IF(GT(READINGTODISTANCE(S2) READINGTODISTANCE(S2)) GT(READINGTODISTANCE(S2) READINGTODISTANCE(S0)) IF(GT(1.0 0.0) GT(1.0 1.0) GT(0.0 0.0))) IF(GT(READINGTODISTANCE(S0) READINGTODISTANCE(S0)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00))) IF(GT(READINGTODISTANCE(S0) READINGTODISTANCE(S1)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00))))))";
            String yay = "WHEELDRIVEFROMCOORD(COORDINATEFROMDISTANCEANDBEARING(IF(GT(IF(GT(0.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)) IF(GT(0.0 1.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2))) IF(IF(GT(1.0 1.0) GT(1.0 0.0) GT(1.0 0.0)) IF(GT(1.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S1)) IF(GT(0.0 0.0) READINGTODISTANCE(S0) READINGTODISTANCE(S1))) IF(IF(GT(0.0 1.0) GT(0.0 0.0) GT(0.0 1.0)) IF(GT(1.0 0.0) READINGTODISTANCE(S1) READINGTODISTANCE(S1)) IF(GT(1.0 1.0) READINGTODISTANCE(S1) READINGTODISTANCE(S0)))) IF(IF(GT(READINGTODISTANCE(S2) READINGTODISTANCE(S2)) GT(READINGTODISTANCE(S2) READINGTODISTANCE(S0)) IF(GT(1.0 0.0) GT(1.0 1.0) GT(0.0 0.0))) IF(GT(READINGTODISTANCE(S0) READINGTODISTANCE(S0)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00))) IF(GT(READINGTODISTANCE(S0) READINGTODISTANCE(S1)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B.00))))))";
            String woo = "WHEELDRIVEFROMCOORD(ROTATECOORDINATE(ROTATECOORDINATE(COORDINATEFROMDISTANCEANDBEARING(READINGTODISTANCE(S0) B.00) B.00) B3.14))";
            String stable10k = "IF(IF(IF(GT(READINGTODISTANCE(S2) READINGTODISTANCE(S0)) IF(GT(0.0 1.0) GT(0.0 0.0) GT(1.0 1.0)) IF(GT(1.0 1.0) GT(0.0 1.0) GT(1.0 0.0))) IF(IF(GT(1.0 1.0) GT(1.0 0.0) GT(1.0 0.0)) IF(GT(1.0 1.0) GT(0.0 1.0) GT(1.0 0.0)) GT(READINGTODISTANCE(S1) READINGTODISTANCE(S2))) GT(IF(GT(0.0 0.0) READINGTODISTANCE(S0) READINGTODISTANCE(S0)) IF(GT(1.0 0.0) READINGTODISTANCE(S2) READINGTODISTANCE(S2)))) WHEELDRIVEFROMCOORD(IF(IF(GT(1.0 1.0) GT(1.0 1.0) GT(1.0 1.0)) IF(GT(1.0 0.0) COORDINATEFROMDISTANCEANDBEARING(1.0 B.00) COORDINATEFROMDISTANCEANDBEARING(1.0 B.00)) IF(GT(0.0 1.0) COORDINATEFROMDISTANCEANDBEARING(1.0 B.00) COORDINATEFROMDISTANCEANDBEARING(0.0 B.00)))) WHEELDRIVEFROMCOORD(ROTATECOORDINATE(IF(GT(0.0 0.0) COORDINATEFROMDISTANCEANDBEARING(0.0 B.00) COORDINATEFROMDISTANCEANDBEARING(1.0 B3.14)) BEARINGFROMCOORDINATE(COORDINATEFROMDISTANCEANDBEARING(1.0 B3.14)))))";
            Node root = model.getParser().parse(stable10k);
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

    private static class ResourceProximitySensor extends ProximityAgentSensor {

        public ResourceProximitySensor(float bearing) {
            super(bearing);
        }

        public ResourceProximitySensor(float bearing, float orientation, float range, float fieldOfView) {
            super(bearing, orientation, range, fieldOfView);
        }

        @Override
        public boolean isRelevantObject(Fixture otherFixture) {
            return otherFixture.getBody().getUserData() instanceof ResourceObject;
        }

        @Override
        protected boolean filterOutObject(PhysicalObject object) {
            if(object instanceof ResourceObject) return ((ResourceObject) object).isCollected();
            else return false;
        }
    }

    public String getExperimentConfig() { return experimentConfig; }
    public String getSimulationConfig() { return simulationConfig; }
    public boolean showVisuals() { return showVisuals; }


}
