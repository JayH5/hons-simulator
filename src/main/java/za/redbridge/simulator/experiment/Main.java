package za.redbridge.simulator.experiment;

import org.epochx.gp.op.init.RampedHalfAndHalfInitialiser;
import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.life.GenerationListener;
import org.epochx.life.Life;
import org.epochx.op.selection.TournamentSelector;
import org.epochx.representation.CandidateProgram;
import org.epochx.stats.Stat;
import org.epochx.stats.StatField;
import org.epochx.stats.Stats;
import org.epochx.tools.eval.MalformedProgramException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import sim.display.Console;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.SimulationGUI;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HeterogeneousRobotFactory;
import za.redbridge.simulator.gp.AgentModel;
import za.redbridge.simulator.gp.CustomStatFields;
import za.redbridge.simulator.khepera.BottomProximitySensor;
import za.redbridge.simulator.khepera.UltrasonicSensor;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.phenotype.GPPhenotype;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.TypedProximityAgentSensor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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

    @Option (name="--population", aliases="-p", usage="GP population pool size")
    private int popSize = 1000;

    @Option (name="--tournament-size", aliases="-t", usage="GP Tournament size")
    private int tournSize = 7;

    @Option (name="--filename", aliases="-f", usage="Filename for the output")
    private String filename;

    private static Stat[] fields = {StatField.GEN_NUMBER, CustomStatFields.RUN_TEAM_FITNESS_MIN, StatField.RUN_FITNESS_MIN, CustomStatFields.GEN_TEAM_FITNESS_MIN, StatField.GEN_FITNESS_MIN, StatField.GEN_FITNESS_AVE, StatField.GEN_FITNESS_MAX, StatField.GEN_FITNESS_STDEV};
    private static String[] fieldLabels = {"Generation", "Run Max Team", "Run Max Individual", "Max Team", "Max Individual", "Avg Individual", "Min Individual", "Std Dev", "Distinct Individuals"};
    private static String fieldFormat = "%-10d,   %-12.2f,   %-18.2f,   %-8.2f,   %-14.2f,   %-14.2f,   %-14.2f,   %-7.2f,   %-20d";

    public static void main (String[] args) throws MalformedProgramException, FileNotFoundException, IOException {

        Main options = new Main();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException c) {
            System.err.println("Error parsing command-line arguments.");
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
            System.err.println("Error parsing morphology file.");
            p.printStackTrace();
        }

        List<AgentSensor> sensors = new ArrayList<>();
        //sensors.add(new UltrasonicSensor(0f, 0f));
        //sensors.add(new UltrasonicSensor((float)Math.PI/2, 0f));
        //sensors.add(new UltrasonicSensor((float)Math.PI, 0f));
        //sensors.add(new UltrasonicSensor((float)(3*Math.PI/2), 0f));

        List<Class> detectables = new ArrayList<>();
        detectables.add(RobotObject.class);
        detectables.add(ResourceObject.class);
        detectables.add(WallObject.class);

        //sensors.add(new TypedProximityAgentSensor(detectables, (float)Math.PI/4, 0.0f, 0.02f, 0.2f));
        //sensors.add(new TypedProximityAgentSensor(detectables, (float)(7*Math.PI/4), 0.0f, 0.02f, 0.2f));

        sensors.add(new UltrasonicSensor((float)(0*Math.PI)/8, 0.0f));
        sensors.add(new UltrasonicSensor((float)(1*Math.PI)/8, 0.0f));
        sensors.add(new UltrasonicSensor((float)(2*Math.PI)/8, 0.0f));
        sensors.add(new UltrasonicSensor((float)(3*Math.PI)/8, 0.0f));
        sensors.add(new UltrasonicSensor((float)(4*Math.PI)/8, 0.0f));
        sensors.add(new UltrasonicSensor((float)(5*Math.PI)/8, 0.0f));
        sensors.add(new UltrasonicSensor((float)(6*Math.PI)/8, 0.0f));
        sensors.add(new UltrasonicSensor((float)(7*Math.PI)/8, 0.0f));

        sensors.add(new BottomProximitySensor());

        AgentModel model = new AgentModel(sensors, simulationConfiguration, experimentConfiguration);
        model.setNoGenerations(200);
        model.setMaxInitialDepth(5);
        model.setMaxDepth(7);
        model.setPopulationSize(options.popSize);
        model.setPoolSize(model.getPopulationSize() / 2);
        model.setProgramSelector(new TournamentSelector(model, options.tournSize));
        model.setNoRuns(1);
        model.setNoElites(model.getPopulationSize() / 4);
        model.setInitialiser(new RampedHalfAndHalfInitialiser(model));
        model.setMutationProbability(0.1);
        model.setCrossoverProbability(0.9);
        model.setTerminationFitness(Double.NEGATIVE_INFINITY);
        if(options.filename == null) {
            System.err.println("Error: No output filename specified in arguments!");
            System.exit(1);
        }
        FileWriter csvWriter = new FileWriter(options.filename + ".csv");
        FileWriter treeWriter = new FileWriter(options.filename + ".trees");
        class GenerationTrackingListener implements GenerationListener{
            private int counter = 0;
            @Override
            public void onGenerationEnd() {
                try {
                    Stats s = Stats.get();
                    //RUN_FITNESS_MIN is not first populated until updateBestProgram in RunManager fires, but this is not how the first iteration gets evaluated
                    //it's evaluated when GEN_FITNESS_MIN is requested; so, we request it, and set the RUN_FITNESS_MIN ourselves
                    if (counter == 0) s.addData(StatField.RUN_FITNESS_MIN, s.getStat(StatField.GEN_FITNESS_MIN));
                    List<Object> printables = Arrays.asList(fields).stream().map(f -> s.getStat(f)).collect(Collectors.toList());

                    List<CandidateProgram> pop = (List<CandidateProgram>) s.getStat(StatField.GEN_POP_SORTED_DESC);
                    List<String> distinctPop = (List<String>) pop.stream().map(c -> c.toString()).distinct().collect(Collectors.toList());
                    printables.add(distinctPop.size());
                    csvWriter.write(String.format(fieldFormat, printables.toArray()) + "\n");

                    List<CandidateProgram> bestTeam = (List<CandidateProgram>) s.getStat(CustomStatFields.GEN_FITTEST_TEAM);
                    treeWriter.write(StatField.GEN_NUMBER + "\t");
                    treeWriter.write("{\"" + bestTeam.stream().map(o -> o.toString()).collect(Collectors.joining("\", \"")) + "\"}\n");

                    //stop if diversity is low enough
                    if ((Double) s.getStat(StatField.GEN_FITNESS_STDEV) < 0.2 && distinctPop.size() < pop.size() / 4.0) {
                        System.out.println("Diversity below threshold; stopping.");
                        model.setTerminationFitness(0.0);
                    }
                    csvWriter.flush();
                    treeWriter.flush();
                    counter++;
                }
                catch(IOException e){
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            @Override
            public void onGenerationStart(){}
        }
        Life.get().addGenerationListener(new GenerationTrackingListener());

        //if we need to show a visualisation
        if (options.showVisuals()) {
            String[] trees = {"WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(BS8 RESOURCE RESOURCE)) READINGISOFTYPE(IF(BS8 TS7 IF(READINGISOFTYPE(TS6 ROBOT) IF(BS8 TS0 TS2) IF(BS8 TS6 TS1))) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS7) IF(BS8 TS1 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(BS8 RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS7 TS6) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS0) IF(BS8 TS4 TS7) TS2)) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) BS8 READINGISOFTYPE(IF(BS8 TS7 TS6) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS0) IF(BS8 TS4 TS7) IF(BS8 TS1 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMBEARING(BEARINGFROMCOORDINATE(READINGTOCOORDINATE(IF(READINGISOFTYPE(TS6 ROBOT) IF(BS8 TS0 TS2) IF(BS8 TS4 TS1)))))", "WHEELDRIVEFROMBEARING(BEARINGFROMCOORDINATE(READINGTOCOORDINATE(IF(READINGISOFTYPE(TS6 IF(BS8 RESOURCE ROBOT)) IF(BS8 TS0 TS2) IF(BS8 TS6 TS1)))))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(BS8 RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS7 IF(READINGISOFTYPE(TS6 ROBOT) IF(BS8 TS0 TS2) IF(BS8 TS6 TS1))) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS7) IF(BS8 TS1 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(TS7) B2.36)))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(BS8 RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS7 TS6) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS6) IF(BS8 TS1 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) BS8 READINGISOFTYPE(IF(BS8 TS7 IF(READINGISOFTYPE(TS6 ROBOT) IF(BS8 TS0 TS2) IF(BS8 TS6 TS1))) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS7) IF(BS8 TS6 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(BS8 RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS7 TS6) IF(BS8 ROBOT WALL))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS7) IF(BS8 TS1 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMBEARING(BEARINGFROMCOORDINATE(READINGTOCOORDINATE(TS6)))", "WHEELDRIVEFROMCOORD(ROTATECOORDINATE(IF(BS8 IF(IF(BS8 BS8 BS8) READINGTOCOORDINATE(TS1) READINGTOCOORDINATE(TS3)) IF(IF(BS8 BS8 BS8) READINGTOCOORDINATE(TS0) READINGTOCOORDINATE(TS3))) B1.57))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS7 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(BS8 RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS7 TS6) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS0) IF(BS8 TS4 TS7) IF(BS8 TS1 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMBEARING(BEARINGFROMCOORDINATE(READINGTOCOORDINATE(TS5)))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(READINGPRESENT(TS2) RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS7 TS6) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS7) IF(BS8 TS1 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(BS8 RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS5 TS6) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS7) IF(BS8 TS1 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMBEARING(BEARINGFROMCOORDINATE(READINGTOCOORDINATE(IF(READINGISOFTYPE(IF(BS8 TS6 TS0) ROBOT) IF(BS8 TS0 TS2) IF(BS8 TS6 TS1)))))", "WHEELDRIVEFROMCOORD(ROTATECOORDINATE(IF(READINGISOFTYPE(TS6 WALL) IF(IF(BS8 BS8 BS8) READINGTOCOORDINATE(TS1) READINGTOCOORDINATE(TS3)) IF(IF(BS8 BS8 BS8) READINGTOCOORDINATE(TS1) READINGTOCOORDINATE(TS3))) B1.57))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS0) IF(BS8 RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS7 IF(READINGISOFTYPE(TS6 ROBOT) IF(BS8 TS0 TS2) IF(BS8 TS6 TS1))) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS7) IF(BS8 TS6 TS3))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMCOORD(IF(IF(IF(READINGISOFTYPE(TS6 NONE) IF(BS8 BS8 BS8) IF(BS8 BS8 BS8)) READINGISOFTYPE(IF(BS8 TS3 TS2) IF(BS8 RESOURCE ROBOT)) READINGISOFTYPE(IF(BS8 TS7 TS6) IF(BS8 ROBOT NONE))) READINGTOCOORDINATE(IF(READINGPRESENT(TS1) IF(BS8 TS4 TS7) IF(BS8 TS6 TS1))) ROTATECOORDINATE(READINGTOCOORDINATE(IF(BS8 TS6 TS0)) B2.36)))", "WHEELDRIVEFROMBEARING(BEARINGFROMCOORDINATE(READINGTOCOORDINATE(IF(READINGISOFTYPE(TS3 ROBOT) IF(BS8 TS0 TS2) IF(BS8 TS6 TS1)))))"};
            /*
            try {
                FileWriter fw = new FileWriter("tree.dot");
                fw.write(new EpoxRenderer().dotRender(root));
                fw.close();
            }catch(IOException e){
                System.err.print("Could not write dot file");
            }
            */
            List<Phenotype> phenotypes = new ArrayList<>();
            for(String t : trees){
                GPPhenotype p = new GPPhenotype(sensors.stream().map(sen -> sen.clone()).collect(Collectors.toList()),new GPCandidateProgram(model.getParser().parse(t), model), model.getInputs());
                phenotypes.add(p);
            }
            HeterogeneousRobotFactory robotFactory = new HeterogeneousRobotFactory(phenotypes, simulationConfiguration.getRobotMass(),
                    simulationConfiguration.getRobotRadius(), simulationConfiguration.getRobotColour());

            Simulation simulation = new Simulation(simulationConfiguration, robotFactory);
            SimulationGUI video = new SimulationGUI(simulation);

            //new console which displays this simulation
            Console console = new Console(video);
            console.setVisible(true);
        }
        else {
            csvWriter.write(Arrays.asList(fieldLabels).stream().collect(Collectors.joining(",   ")));
            csvWriter.flush();
            //headless option
            model.run();
            csvWriter.close();
            treeWriter.close();
        }

    }

    public String getExperimentConfig() { return experimentConfig; }
    public String getSimulationConfig() { return simulationConfig; }
    public boolean showVisuals() { return showVisuals; }


}
