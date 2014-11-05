package za.redbridge.simulator.experiment;

import org.epochx.epox.Node;
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
import za.redbridge.simulator.gp.EpoxRenderer;
import za.redbridge.simulator.khepera.BottomProximitySensor;
import za.redbridge.simulator.khepera.UltrasonicSensor;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.phenotype.GPPhenotype;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.TypedProximityAgentSensor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

//entry point into simulator

/**
 * Created by racter on 2014/08/19.
 */
public class Main {

    //config files for this experiment

    @Option(name="--experiment-config", usage="Filename for experiment configuration", metaVar="<experiment config>")
    private String experimentConfig = "configs/experimentConfig.yml";

    @Option (name="--simulation-config", usage="Filename for simulation configuration", metaVar="<simulation config>")
    private String simulationConfig = "configs/mediumSimConfig.yml";

    @Option (name="--morphology-config", usage="Filename for morphology configuration", metaVar="<morphology config>")
    private String morphologyConfig = "morphs/3ult.yml";

    @Option (name="--show-visuals", aliases="-v", usage="Show visualisation for simulation")
    private boolean showVisuals = false;

    @Option (name="--population", aliases="-p", usage="GP population pool size")
    private int popSize = 700;

    @Option (name="--tournament-size", aliases="-t", usage="GP Tournament size")
    private int tournSize = 7;

    @Option (name="--generation-limit", aliases="-g", usage="Generation limit")
    private int genLimit = 70;

    @Option (name="--run-index", aliases="-r", usage="Index of this run")
    private Integer runIndex = 0;

    @Option (name="--output-dir", aliases="-o", usage="Output directory")
    private String outputDir = "results";

    private static Stat[] fields = {StatField.GEN_NUMBER, CustomStatFields.RUN_TEAM_FITNESS_MIN, CustomStatFields.GEN_TEAM_FITNESS_MIN, StatField.GEN_FITNESS_MIN, StatField.GEN_FITNESS_AVE, StatField.GEN_FITNESS_MAX, StatField.GEN_FITNESS_STDEV};
    private static String[] fieldLabels = {"Generation", "Run Max Team", "Max Team", "Max Individual", "Avg Individual", "Min Individual", "Std Dev", "Distinct Individuals"};
    private static String fieldFormat = "%-10d,   %-12.2f,   %-8.2f,   %-14.2f,   %-14.2f,   %-14.2f,   %-7.2f,   %-20d";

    public static void main (String[] args) throws MalformedProgramException, IOException {

        Main options = new Main();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException c) {
            System.err.println("Error parsing command-line arguments.");
            System.exit(1);
        }

        ExperimentConfig experimentConfiguration = new ExperimentConfig(options.experimentConfig);
        SimConfig simulationConfiguration = new SimConfig(options.simulationConfig);

        MorphologyConfig morphologyConfig = null;

        try {
            morphologyConfig = new MorphologyConfig(options.morphologyConfig);
        }
        catch(ParseException p) {
            System.err.println("Error parsing morphology file.");
            p.printStackTrace();
        }

        List<AgentSensor> sensors = morphologyConfig.getSensorList();

        /*
        List<Class> detectables = new ArrayList<>();
        detectables.add(RobotObject.class);
        detectables.add(ResourceObject.class);
        detectables.add(WallObject.class);

        sensors.add(new TypedProximityAgentSensor(detectables, (float)Math.PI/4, 0.0f, 0.02f, 0.2f));
        sensors.add(new TypedProximityAgentSensor(detectables, (float)(7*Math.PI/4), 0.0f, 0.02f, 0.2f));
        */

        AgentModel model = new AgentModel(sensors, simulationConfiguration, experimentConfiguration);
        model.setNoGenerations(options.genLimit);
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

        //if we need to show a visualisation
        if (options.showVisuals) {
            String[] trees = {"IF(IF(IF(BS0 IF(IF(BS0 BS0 BS0) IF(BS0 BS0 READINGPRESENT(TS7)) READINGISOFTYPE(TS6 NONE)) READINGPRESENT(TS7)) READINGPRESENT(PS3) IF(BS0 IF(IF(READINGPRESENT(PS4) READINGISOFTYPE(TS7 NONE) BS0) BS0 BS0) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(READINGISOFTYPE(TS6 NONE) IF(BS0 BS0 READINGPRESENT(TS5)) BS0) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.03)) IF(IF(IF(IF(IF(READINGISOFTYPE(TS6 NONE) BS0 BS0) BS0 READINGPRESENT(IF(BS0 PS3 PS3))) READINGISOFTYPE(TS7 NONE) BS0) IF(READINGISOFTYPE(TS6 NONE) READINGISOFTYPE(TS7 IF(BS0 NONE NONE)) IF(BS0 IF(BS0 BS0 BS0) BS0)) IF(READINGPRESENT(TS6) GT(READINGTODISTANCE(TS6) FL0.5) READINGPRESENT(TS6))) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00)))", "IF(IF(IF(IF(IF(GT(FL1.0 READINGTODISTANCE(PS4)) BS0 BS0) BS0 IF(BS0 IF(READINGPRESENT(PS4) READINGISOFTYPE(TS6 NONE) BS0) BS0)) IF(GT(READINGTODISTANCE(TS6) IF(BS0 FL1.0 FL0.0)) BS0 IF(BS0 BS0 IF(BS0 BS0 BS0))) READINGPRESENT(TS7)) READINGPRESENT(PS3) IF(IF(BS0 BS0 BS0) IF(IF(BS0 IF(BS0 BS0 BS0) BS0) BS0 IF(GT(FL0.5 READINGTOFLOAT(TS6)) BS0 BS0)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(IF(READINGPRESENT(PS4) BS0 BS0) READINGISOFTYPE(TS8 NONE) BS0) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(READINGPRESENT(TS7) BS0 BS0) IF(BS0 READINGPRESENT(TS6) READINGISOFTYPE(TS8 ROBOT)) IF(BS0 IF(BS0 BS0 BS0) BS0)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00)))", "IF(IF(IF(IF(IF(GT(FL0.0 READINGTODISTANCE(PS3)) BS0 BS0) BS0 IF(IF(BS0 BS0 READINGPRESENT(PS3)) BS0 BS0)) IF(IF(READINGPRESENT(TS5) BS0 BS0) IF(BS0 BS0 READINGPRESENT(TS5)) IF(BS0 READINGPRESENT(TS6) BS0)) IF(READINGPRESENT(PS4) BS0 READINGISOFTYPE(TS7 RESOURCE))) READINGPRESENT(PS2) IF(IF(BS0 IF(BS0 BS0 BS0) BS0) IF(BS0 BS0 BS0) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(IF(READINGPRESENT(TS7) BS0 BS0) IF(READINGPRESENT(PS4) BS0 BS0) IF(READINGISOFTYPE(TS6 WALL) IF(BS0 BS0 BS0) READINGPRESENT(PS4))) IF(BS0 IF(BS0 BS0 READINGISOFTYPE(TS7 NONE)) GT(READINGTODISTANCE(PS3) FL0.5))) B1.57 B.00)) IF(IF(IF(IF(IF(READINGPRESENT(TS6) BS0 READINGISOFTYPE(TS6 ROBOT)) BS0 READINGPRESENT(IF(BS0 PS3 PS4))) BS0 READINGPRESENT(TS8)) READINGPRESENT(PS3) IF(BS0 IF(READINGPRESENT(TS6) BS0 BS0) BS0)) WHEELDRIVEFROMBEARING(IF(IF(BS0 READINGPRESENT(TS7) GT(READINGTODISTANCE(PS3) FL0.0)) B.00 B.00)) WHEELDRIVEFROMBEARING(B.00)))", "IF(IF(IF(IF(BS0 BS0 BS0) BS0 IF(IF(IF(BS0 BS0 BS0) BS0 BS0) BS0 BS0)) READINGPRESENT(PS3) IF(READINGPRESENT(TS7) IF(IF(READINGPRESENT(TS5) READINGISOFTYPE(TS6 NONE) BS0) BS0 IF(IF(READINGPRESENT(PS4) IF(BS0 BS0 BS0) BS0) IF(BS0 BS0 BS0) READINGISOFTYPE(TS6 NONE))) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 BS0 BS0) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(READINGISOFTYPE(TS7 NONE) IF(BS0 BS0 IF(IF(BS0 BS0 BS0) IF(BS0 BS0 BS0) BS0)) BS0) WHEELDRIVEFROMBEARING(IF(IF(BS0 BS0 GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(READINGISOFTYPE(TS7 ROBOT) READINGPRESENT(TS6) BS0) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00))))", "IF(IF(IF(IF(GT(FL0.5 READINGTOFLOAT(TS6)) BS0 READINGPRESENT(TS5)) IF(READINGPRESENT(PS4) BS0 IF(BS0 BS0 BS0)) READINGPRESENT(TS5)) READINGPRESENT(PS3) IF(IF(IF(READINGPRESENT(TS7) BS0 READINGPRESENT(TS7)) READINGPRESENT(TS6) BS0) IF(IF(READINGPRESENT(TS7) READINGPRESENT(TS7) IF(BS0 BS0 BS0)) BS0 IF(BS0 BS0 BS0)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(READINGPRESENT(TS7) BS0 BS0) READINGPRESENT(TS6)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(READINGPRESENT(TS5) BS0 BS0) IF(READINGPRESENT(TS6) BS0 BS0) IF(GT(READINGTODISTANCE(PS3) FL0.5) READINGPRESENT(TS6) BS0)) WHEELDRIVEFROMBEARING(B.00) IF(GT(READINGTODISTANCE(PS3) FL0.5) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(IF(BS0 B.03 B.00)))))", "IF(IF(IF(IF(BS0 BS0 READINGPRESENT(PS4)) IF(IF(READINGPRESENT(TS7) BS0 IF(BS0 BS0 BS0)) READINGPRESENT(PS4) IF(BS0 BS0 BS0)) READINGPRESENT(TS7)) READINGPRESENT(PS3) IF(IF(BS0 BS0 BS0) IF(IF(BS0 READINGPRESENT(PS3) IF(BS0 BS0 READINGPRESENT(PS3))) BS0 IF(BS0 BS0 READINGPRESENT(TS6))) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(READINGPRESENT(TS5) BS0 BS0) READINGPRESENT(TS6)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(IF(BS0 BS0 BS0) BS0 READINGPRESENT(TS6)) IF(BS0 IF(READINGPRESENT(TS6) BS0 READINGISOFTYPE(TS7 ROBOT)) BS0) IF(BS0 IF(READINGPRESENT(PS3) IF(BS0 BS0 BS0) BS0) GT(READINGTODISTANCE(TS6) FL0.5))) WHEELDRIVEFROMBEARING(B.00) IF(IF(READINGPRESENT(TS6) IF(BS0 BS0 BS0) BS0) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(IF(BS0 B1.57 B1.57)))))", "IF(IF(IF(BS0 IF(BS0 IF(BS0 BS0 READINGPRESENT(TS6)) GT(READINGTOFLOAT(PS3) FL0.0)) IF(BS0 IF(READINGPRESENT(PS3) BS0 IF(READINGISOFTYPE(TS6 ROBOT) BS0 BS0)) BS0)) READINGPRESENT(PS3) IF(BS0 IF(BS0 BS0 IF(BS0 READINGISOFTYPE(TS7 IF(BS0 RESOURCE NONE)) BS0)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 BS0 BS0) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(READINGISOFTYPE(TS5 IF(BS0 IF(BS0 NONE NONE) IF(BS0 RESOURCE ROBOT))) IF(READINGPRESENT(TS5) READINGPRESENT(PS4) IF(GT(FL0.0 READINGTOFLOAT(TS6)) BS0 READINGPRESENT(TS5))) BS0) WHEELDRIVEFROMBEARING(B.00) IF(IF(READINGISOFTYPE(TS8 ROBOT) READINGPRESENT(TS6) READINGISOFTYPE(TS6 WALL)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00))))", "IF(IF(IF(IF(IF(IF(BS0 BS0 BS0) BS0 BS0) BS0 BS0) IF(BS0 READINGPRESENT(PS4) IF(BS0 BS0 BS0)) READINGPRESENT(IF(BS0 TS7 TS7))) READINGPRESENT(PS3) IF(BS0 IF(IF(BS0 READINGISOFTYPE(TS7 NONE) BS0) BS0 IF(GT(FL0.5 READINGTODISTANCE(PS3)) BS0 READINGPRESENT(TS5))) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(BS0 READINGISOFTYPE(TS7 NONE) BS0) READINGISOFTYPE(TS5 NONE)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(READINGPRESENT(TS6) IF(IF(IF(BS0 BS0 BS0) BS0 BS0) BS0 BS0) BS0) WHEELDRIVEFROMBEARING(B.00) IF(IF(IF(BS0 BS0 READINGPRESENT(TS6)) IF(BS0 READINGPRESENT(TS7) BS0) READINGISOFTYPE(TS6 NONE)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00))))", "IF(IF(IF(IF(BS0 BS0 BS0) IF(IF(READINGPRESENT(TS7) BS0 READINGPRESENT(TS6)) READINGPRESENT(PS4) IF(BS0 BS0 READINGISOFTYPE(TS7 NONE))) READINGPRESENT(TS7)) READINGPRESENT(PS3) IF(IF(BS0 READINGISOFTYPE(TS5 NONE) BS0) IF(IF(READINGPRESENT(TS7) READINGPRESENT(PS3) IF(BS0 BS0 READINGPRESENT(PS3))) BS0 IF(BS0 BS0 BS0)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(READINGPRESENT(TS5) BS0 BS0) BS0) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(BS0 BS0 READINGPRESENT(TS7)) IF(BS0 BS0 BS0) IF(BS0 BS0 IF(BS0 BS0 BS0))) WHEELDRIVEFROMFLOATS(FL0.5 IF(GT(FL1.0 READINGTODISTANCE(TS7)) READINGTODISTANCE(TS6) READINGTODISTANCE(TS6))) IF(IF(READINGPRESENT(TS5) IF(BS0 BS0 BS0) READINGISOFTYPE(TS8 WALL)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(IF(BS0 B.00 B.00)))))", "IF(IF(IF(IF(GT(FL0.5 READINGTOFLOAT(TS6)) BS0 READINGPRESENT(TS5)) IF(IF(READINGPRESENT(TS7) BS0 IF(BS0 BS0 BS0)) READINGPRESENT(PS4) IF(BS0 BS0 BS0)) READINGPRESENT(TS7)) READINGPRESENT(PS3) IF(IF(IF(READINGPRESENT(TS7) BS0 BS0) BS0 BS0) IF(IF(BS0 READINGPRESENT(PS4) IF(BS0 BS0 READINGPRESENT(PS3))) BS0 IF(BS0 BS0 BS0)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(READINGPRESENT(TS6) BS0 BS0) READINGPRESENT(TS6)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(READINGPRESENT(TS5) BS0 READINGISOFTYPE(TS8 RESOURCE)) IF(BS0 BS0 BS0) IF(BS0 READINGPRESENT(TS6) BS0)) WHEELDRIVEFROMBEARING(B.00) IF(GT(READINGTODISTANCE(PS3) FL0.5) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00))))", "IF(IF(IF(IF(BS0 BS0 READINGPRESENT(PS4)) BS0 READINGPRESENT(TS7)) READINGPRESENT(PS3) IF(IF(IF(BS0 READINGPRESENT(PS4) BS0) BS0 BS0) IF(IF(BS0 READINGPRESENT(PS3) IF(BS0 BS0 READINGPRESENT(PS3))) BS0 IF(READINGPRESENT(PS4) BS0 READINGISOFTYPE(IF(BS0 TS6 TS8) ROBOT))) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 BS0 GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(READINGPRESENT(IF(BS0 PS3 PS3)) IF(IF(BS0 IF(BS0 BS0 BS0) BS0) BS0 READINGPRESENT(PS3)) BS0) WHEELDRIVEFROMBEARING(B.00) IF(IF(READINGISOFTYPE(TS7 NONE) READINGPRESENT(TS7) READINGISOFTYPE(TS7 ROBOT)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00))))", "IF(IF(IF(IF(BS0 BS0 IF(IF(READINGPRESENT(PS3) BS0 READINGPRESENT(PS3)) BS0 READINGISOFTYPE(TS7 WALL))) IF(IF(READINGPRESENT(TS7) BS0 IF(BS0 BS0 BS0)) READINGPRESENT(PS4) IF(BS0 BS0 BS0)) READINGPRESENT(TS7)) READINGPRESENT(PS3) IF(IF(IF(READINGPRESENT(TS6) BS0 BS0) BS0 READINGPRESENT(TS6)) IF(IF(READINGPRESENT(TS7) READINGPRESENT(PS3) BS0) BS0 IF(BS0 BS0 BS0)) IF(BS0 IF(BS0 IF(BS0 BS0 BS0) BS0) READINGPRESENT(PS4)))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 BS0 READINGPRESENT(TS6)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(READINGISOFTYPE(TS6 ROBOT) BS0 BS0) IF(BS0 BS0 BS0) IF(IF(BS0 BS0 IF(BS0 BS0 IF(BS0 BS0 BS0))) READINGPRESENT(TS6) GT(FL0.0 READINGTOFLOAT(PS3)))) WHEELDRIVEFROMBEARING(B.00) IF(IF(READINGPRESENT(TS6) IF(BS0 BS0 BS0) READINGISOFTYPE(TS7 NONE)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00))))", "IF(IF(IF(IF(IF(READINGPRESENT(PS3) BS0 IF(READINGPRESENT(PS3) BS0 BS0)) BS0 READINGISOFTYPE(TS7 WALL)) IF(BS0 READINGPRESENT(PS4) IF(BS0 BS0 BS0)) READINGPRESENT(TS6)) READINGPRESENT(PS3) IF(IF(GT(FL0.0 READINGTODISTANCE(PS3)) BS0 BS0) IF(BS0 BS0 BS0) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(BS0 BS0 BS0) READINGPRESENT(PS3)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(BS0 BS0 GT(READINGTODISTANCE(PS3) FL0.5)) BS0 BS0) IF(IF(IF(BS0 BS0 BS0) IF(BS0 READINGPRESENT(PS3) BS0) READINGISOFTYPE(TS6 NONE)) IF(READINGPRESENT(TS6) WHEELDRIVEFROMBEARING(B.79) WHEELDRIVEFROMBEARING(B.00)) WHEELDRIVEFROMBEARING(B.00)) IF(IF(IF(BS0 IF(BS0 BS0 READINGPRESENT(TS6)) BS0) GT(READINGTODISTANCE(PS3) FL0.5) READINGISOFTYPE(TS6 NONE)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(IF(READINGPRESENT(TS6) B.00 B.00)))))", "IF(IF(IF(IF(IF(BS0 IF(BS0 READINGPRESENT(PS4) IF(BS0 BS0 BS0)) BS0) BS0 BS0) IF(BS0 READINGPRESENT(PS4) IF(BS0 IF(READINGPRESENT(TS5) BS0 BS0) BS0)) READINGPRESENT(TS6)) READINGPRESENT(PS3) IF(IF(IF(IF(BS0 BS0 READINGPRESENT(PS4)) IF(BS0 READINGPRESENT(PS4) BS0) READINGPRESENT(TS7)) BS0 IF(READINGPRESENT(TS5) BS0 BS0)) IF(IF(READINGISOFTYPE(TS7 ROBOT) IF(IF(BS0 BS0 BS0) BS0 BS0) BS0) BS0 READINGPRESENT(PS3)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(BS0 BS0 READINGPRESENT(PS4)) READINGPRESENT(PS4)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(BS0 BS0 GT(READINGTODISTANCE(PS3) FL0.5)) READINGISOFTYPE(TS6 RESOURCE) BS0) WHEELDRIVEFROMBEARING(B.00) IF(IF(IF(BS0 IF(BS0 IF(BS0 BS0 BS0) READINGPRESENT(PS3)) READINGPRESENT(PS3)) GT(READINGTODISTANCE(PS4) FL0.5) READINGISOFTYPE(TS6 NONE)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(IF(READINGPRESENT(TS6) B.00 B.00)))))", "IF(IF(IF(IF(IF(READINGPRESENT(PS4) BS0 BS0) BS0 BS0) READINGPRESENT(PS4) READINGPRESENT(TS6)) READINGPRESENT(PS3) IF(IF(BS0 IF(BS0 BS0 BS0) BS0) IF(BS0 IF(BS0 BS0 READINGPRESENT(TS6)) BS0) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 BS0 IF(READINGISOFTYPE(TS5 ROBOT) IF(BS0 BS0 BS0) BS0)) IF(BS0 IF(BS0 IF(BS0 BS0 BS0) BS0) GT(READINGTODISTANCE(PS3) FL0.5))) B1.57 B.00)) IF(IF(IF(IF(IF(READINGISOFTYPE(TS6 NONE) BS0 BS0) BS0 READINGPRESENT(IF(BS0 PS3 PS4))) READINGISOFTYPE(TS7 NONE) BS0) IF(READINGISOFTYPE(TS6 NONE) READINGISOFTYPE(TS6 IF(BS0 RESOURCE NONE)) IF(READINGPRESENT(TS6) BS0 BS0)) IF(READINGPRESENT(TS6) GT(READINGTODISTANCE(TS6) FL0.5) READINGPRESENT(TS7))) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00)))", "IF(IF(IF(BS0 IF(BS0 IF(BS0 BS0 READINGPRESENT(TS6)) GT(READINGTODISTANCE(PS3) FL0.0)) IF(BS0 IF(READINGPRESENT(PS3) BS0 BS0) BS0)) READINGPRESENT(PS3) IF(BS0 IF(READINGPRESENT(TS6) BS0 IF(BS0 READINGISOFTYPE(TS7 NONE) BS0)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 BS0 BS0) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(READINGISOFTYPE(TS5 IF(BS0 IF(BS0 NONE NONE) IF(BS0 RESOURCE ROBOT))) IF(READINGPRESENT(TS5) READINGPRESENT(PS4) IF(GT(FL0.0 READINGTOFLOAT(TS6)) BS0 READINGPRESENT(TS5))) BS0) WHEELDRIVEFROMBEARING(B.00) IF(IF(READINGISOFTYPE(TS7 IF(BS0 NONE NONE)) READINGPRESENT(TS6) READINGISOFTYPE(TS6 WALL)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00))))", "IF(IF(IF(IF(IF(BS0 IF(BS0 READINGPRESENT(PS4) IF(BS0 BS0 BS0)) BS0) BS0 IF(BS0 IF(BS0 BS0 BS0) READINGISOFTYPE(TS6 NONE))) IF(BS0 READINGPRESENT(PS4) IF(BS0 IF(READINGPRESENT(TS5) BS0 BS0) BS0)) READINGPRESENT(TS6)) READINGPRESENT(PS3) IF(IF(IF(IF(BS0 BS0 READINGPRESENT(PS4)) IF(BS0 READINGPRESENT(PS4) BS0) READINGPRESENT(TS7)) BS0 IF(READINGPRESENT(TS6) BS0 BS0)) IF(IF(READINGISOFTYPE(TS7 ROBOT) IF(BS0 BS0 BS0) BS0) BS0 BS0) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(BS0 BS0 READINGPRESENT(PS4)) READINGPRESENT(PS4)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(BS0 BS0 GT(READINGTODISTANCE(PS3) FL0.5)) READINGISOFTYPE(TS6 RESOURCE) BS0) WHEELDRIVEFROMBEARING(B.00) IF(IF(IF(BS0 IF(BS0 IF(BS0 BS0 BS0) READINGPRESENT(PS3)) READINGPRESENT(PS3)) GT(READINGTODISTANCE(PS3) FL0.5) READINGISOFTYPE(TS6 NONE)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(IF(READINGPRESENT(TS6) B.00 B.00)))))", "IF(IF(IF(IF(IF(BS0 IF(BS0 READINGPRESENT(PS4) IF(BS0 BS0 BS0)) BS0) BS0 BS0) IF(BS0 READINGPRESENT(PS4) IF(BS0 IF(READINGPRESENT(TS5) BS0 BS0) BS0)) READINGPRESENT(TS6)) READINGPRESENT(PS3) IF(IF(IF(IF(BS0 BS0 READINGPRESENT(PS4)) IF(BS0 READINGPRESENT(PS4) BS0) READINGPRESENT(TS7)) BS0 IF(READINGPRESENT(TS5) BS0 BS0)) IF(IF(READINGISOFTYPE(TS7 ROBOT) IF(IF(BS0 BS0 BS0) BS0 BS0) BS0) BS0 READINGPRESENT(PS3)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 IF(BS0 BS0 READINGPRESENT(PS4)) READINGPRESENT(PS4)) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(BS0 BS0 GT(READINGTODISTANCE(PS3) FL0.5)) READINGISOFTYPE(TS6 RESOURCE) BS0) WHEELDRIVEFROMBEARING(B.00) IF(IF(IF(BS0 IF(BS0 IF(BS0 BS0 BS0) READINGPRESENT(PS3)) READINGPRESENT(PS3)) GT(READINGTODISTANCE(PS3) FL0.5) READINGISOFTYPE(TS6 NONE)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(IF(READINGPRESENT(TS6) B.00 B.00)))))", "IF(IF(IF(BS0 IF(BS0 IF(BS0 BS0 READINGPRESENT(TS6)) GT(READINGTODISTANCE(PS4) FL0.0)) IF(READINGISOFTYPE(TS8 RESOURCE) IF(READINGPRESENT(PS3) BS0 BS0) IF(READINGISOFTYPE(TS6 NONE) BS0 BS0))) READINGPRESENT(PS3) IF(BS0 IF(IF(BS0 READINGISOFTYPE(IF(BS0 TS6 TS8) NONE) BS0) BS0 IF(BS0 READINGISOFTYPE(TS7 NONE) BS0)) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 IF(BS0 BS0 BS0) GT(READINGTODISTANCE(PS3) FL0.5)) B1.57 B.00)) IF(IF(IF(BS0 BS0 GT(READINGTODISTANCE(PS3) FL0.5)) IF(READINGPRESENT(TS5) READINGPRESENT(PS4) IF(GT(FL0.0 READINGTOFLOAT(TS6)) BS0 READINGPRESENT(TS5))) BS0) WHEELDRIVEFROMBEARING(IF(IF(BS0 READINGPRESENT(TS7) GT(READINGTODISTANCE(PS3) FL0.0)) B.00 B.00)) IF(IF(READINGISOFTYPE(TS7 ROBOT) READINGPRESENT(TS6) READINGISOFTYPE(TS6 WALL)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(B.00))))", "IF(IF(IF(BS0 IF(BS0 READINGPRESENT(PS4) IF(BS0 READINGPRESENT(TS6) BS0)) READINGPRESENT(TS6)) READINGPRESENT(PS3) IF(IF(IF(READINGPRESENT(TS5) BS0 BS0) BS0 BS0) IF(IF(BS0 READINGPRESENT(PS3) READINGPRESENT(PS4)) BS0 IF(BS0 BS0 READINGISOFTYPE(IF(BS0 TS6 TS8) NONE))) READINGPRESENT(PS4))) WHEELDRIVEFROMBEARING(IF(IF(BS0 BS0 GT(READINGTODISTANCE(PS3) FL0.0)) B1.57 B.00)) IF(IF(READINGISOFTYPE(TS7 NONE) READINGPRESENT(TS7) IF(BS0 BS0 IF(BS0 BS0 BS0))) WHEELDRIVEFROMBEARING(B.00) IF(IF(READINGPRESENT(TS6) BS0 READINGPRESENT(TS7)) WHEELDRIVEFROMBEARING(B.00) WHEELDRIVEFROMBEARING(IF(BS0 B.03 IF(BS0 B.03 B.00))))))"};
            Node root = model.getParser().parse(trees[0]);
            try {
                FileWriter fw = new FileWriter("tree.dot");
                fw.write(new EpoxRenderer().dotRender(root));
                fw.close();
            }catch(IOException e){
                System.err.print("Could not write dot file");
            }
            List<Phenotype> phenotypes = new ArrayList<>();
            for (String t : trees) {
                GPPhenotype p = new GPPhenotype(sensors.stream().map(sen -> sen.clone()).collect(Collectors.toList()), new GPCandidateProgram(model.getParser().parse(t), model), model.getInputs());
                phenotypes.add(p);
            }
            HeterogeneousRobotFactory robotFactory = new HeterogeneousRobotFactory(phenotypes, simulationConfiguration.getRobotMass(),
                    simulationConfiguration.getRobotRadius(), simulationConfiguration.getRobotColour());

            Simulation simulation = new Simulation(simulationConfiguration, robotFactory);
            SimulationGUI video = new SimulationGUI(simulation);

            //new console which displays this simulation
            Console console = new Console(video);
            console.setVisible(true);
        } else {
            if(options.runIndex == null){
                throw new IllegalArgumentException("Run index was not provided in the arguments");
            }

            String morphName = Paths.get(options.morphologyConfig).getFileName().toString().split("\\.")[0];
            String simSize = Paths.get(options.simulationConfig).getFileName().toString().split("SimConfig")[0];
            String outputFilename = morphName + "-p" + options.popSize + "-t" + options.tournSize + "-" + simSize + "-r" + options.runIndex;
            Files.createDirectories(Paths.get(options.outputDir));
            Path csvOutputPath = Paths.get(options.outputDir).resolve(outputFilename + ".csv");
            Path treeOutputPath = Paths.get(options.outputDir).resolve(outputFilename + ".trees");
            BufferedWriter csvWriter = Files.newBufferedWriter(csvOutputPath);
            BufferedWriter treeWriter = Files.newBufferedWriter(treeOutputPath);

            class GenerationTrackingListener implements GenerationListener {
                private int counter = 0;

                @Override
                public void onGenerationStart() {
                    if (counter == 0) {
                        counter++;
                        return;
                    }
                    try {
                        Stats s = Stats.get();
                        s.getStat(StatField.GEN_FITNESS_MIN);
                        List<Object> printables = Arrays.asList(fields).stream().map(f -> s.getStat(f)).collect(Collectors.toList());

                        List<CandidateProgram> pop = (List<CandidateProgram>) s.getStat(StatField.GEN_POP_SORTED_DESC);
                        List<String> distinctPop = (List<String>) pop.stream().map(c -> c.toString()).distinct().collect(Collectors.toList());
                        printables.add(distinctPop.size());
                        csvWriter.write(String.format(fieldFormat, printables.toArray()) + "\n");

                        List<CandidateProgram> bestTeam = (List<CandidateProgram>) s.getStat(CustomStatFields.GEN_FITTEST_TEAM);
                        treeWriter.write(s.getStat(StatField.GEN_NUMBER) + ",   ");
                        treeWriter.write("{\"" + bestTeam.stream().map(o -> o.toString()).collect(Collectors.joining("\", \"")) + "\"}\n");

                        csvWriter.flush();
                        treeWriter.flush();
                        counter++;
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

                @Override
                public void onGenerationEnd() {
                }
            }

            Life.get().addGenerationListener(new GenerationTrackingListener());

            csvWriter.write(Arrays.asList(fieldLabels).stream().collect(Collectors.joining(",   ")) + "\n");
            csvWriter.flush();

            model.run();
            csvWriter.close();
            treeWriter.close();
            System.out.println("Completed run; written to " + outputFilename + "{.csv,.trees}");
        }
    }
}
