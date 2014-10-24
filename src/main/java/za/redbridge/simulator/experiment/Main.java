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
    private String experimentConfig;

    @Option (name="--simulation-config", usage="Filename for simulation configuration", metaVar="<simulation config>")
    private String simulationConfig;

    @Option (name="--morphology-config", usage="Filename for morphology configuration", metaVar="<morphology config>")
    private String morphologyConfig;

    @Option (name="--show-visuals", aliases="-v", usage="Show visualisation for simulation")
    private boolean showVisuals = false;

    @Option (name="--population", aliases="-p", usage="GP population pool size")
    private int popSize = 700;

    @Option (name="--tournament-size", aliases="-t", usage="GP Tournament size")
    private int tournSize = 7;

    @Option (name="--generation-limit", aliases="-g", usage="Generation limit")
    private int genLimit = 70;

    @Option (name="--run-index", aliases="-r", usage="Index of this run")
    private Integer runIndex;

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
            String[] trees = {"WHEELDRIVEFROMBEARING(RANDOMBEARING())","WHEELDRIVEFROMBEARING(RANDOMBEARING())","WHEELDRIVEFROMBEARING(RANDOMBEARING())","WHEELDRIVEFROMBEARING(RANDOMBEARING())"};
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
            Path csvOutputPath = Paths.get(options.outputDir).resolve(outputFilename + ".csv");
            Path treeOutputPath = Paths.get(options.outputDir).resolve(outputFilename + ".trees");
            System.out.println("Writing run to " + outputFilename + "{.csv,.trees}");
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

                        //stop if diversity is low enough
                        if ((Double) s.getStat(StatField.GEN_FITNESS_STDEV) < 0.2 && distinctPop.size() < pop.size() / 4.0) {
                            System.out.println("Diversity below threshold; stopping.");
                            model.setTerminationFitness(0.0);
                        }
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
        }
    }
}
