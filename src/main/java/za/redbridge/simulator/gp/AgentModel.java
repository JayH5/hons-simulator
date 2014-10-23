package za.redbridge.simulator.gp;

import org.apache.commons.math3.util.Pair;
import org.epochx.epox.EpoxParser;
import org.epochx.epox.Literal;
import org.epochx.epox.Node;
import org.epochx.epox.Variable;
import org.epochx.epox.lang.IfFunction;
import org.epochx.gp.model.GPModel;
import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.representation.CandidateProgram;
import org.epochx.stats.StatField;
import org.epochx.stats.Stats;
import za.redbridge.simulator.FitnessStats;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HeterogeneousRobotFactory;
import za.redbridge.simulator.gp.functions.*;
import za.redbridge.simulator.gp.types.*;
import za.redbridge.simulator.khepera.BottomProximitySensor;
import za.redbridge.simulator.phenotype.GPPhenotype;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.TypedProximityAgentSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static za.redbridge.simulator.Utils.sumLists;

/**
 * Created by xenos on 9/9/14.
 */
public class AgentModel extends GPModel {
    //number of simulations run per phenotype evaluation
    private List<AgentSensor> sensors;
    private final SimConfig config;
    private final ExperimentConfig exConfig;
    private final EpoxParser parser = new EpoxParser();

    List<Variable> inputs = new ArrayList<>();
    protected final static float P2 = (float) Math.PI/2;

   public AgentModel(List<AgentSensor> sensors, SimConfig config, ExperimentConfig exConfig){
       this.sensors = sensors;
       this.config = config;
       this.exConfig = exConfig;
       List<Node> syntax = new ArrayList<>();
       for(int i = 0; i < sensors.size(); i++){
           AgentSensor sensor = sensors.get(i);
           if(sensor instanceof BottomProximitySensor)  inputs.add(new ThresholdedSensorVariable("BS" + i, 0.5f));
           else if(sensor instanceof TypedProximityAgentSensor) {
               //we get the list of detectable object types from the sensor class list
               List<DetectedObject.Type> dob = ((TypedProximityAgentSensor)sensor).getSenseClasses().stream().map(DetectedObject::fromClass).collect(Collectors.toList());
               inputs.add(new TypedProximitySensorVariable("TS" + i, dob, sensor));
               syntax.add(new Literal(new Bearing(sensor.getBearing())));
           }
           else{
               inputs.add(new ProximitySensorVariable("PS" + i, Float.class, sensors.get(i)));
               syntax.add(new Literal(new Bearing(sensor.getBearing())));
           }
       }

       /*
       syntax.add(new AddFunction());
       syntax.add(new SubtractFunction());
       syntax.add(new DivisionProtectedFunction());
       syntax.add(new MultiplyFunction());
       */

       /*
       syntax.add(new SineFunction());
       syntax.add(new CosineFunction());
       syntax.add(new TangentFunction());
       syntax.add(new ArcTangentFunction());
       */
       for(DetectedObject.Type t : DetectedObject.Type.values()){
           syntax.add(new Literal(t));
       }
       syntax.add(new ReadingIsOfType());

       syntax.add(new IfFunction());
       syntax.add(new GT());

       syntax.add(new Literal(new Bearing(0.0f)));
       syntax.add(new Literal(new Bearing(P2)));
       syntax.add(new Literal(new Bearing(2*P2)));
       syntax.add(new Literal(new Bearing(3*P2)));

       syntax.add(new Literal(new GPFloatLiteral(0.0f)));
       syntax.add(new Literal(new GPFloatLiteral(0.5f)));
       syntax.add(new Literal(new GPFloatLiteral(1.0f)));
       //syntax.add(new RandomGPFloat());
       //syntax.add(new Literal(new FloatLiteral(2.0f)));
       //syntax.add(new Literal(new FloatLiteral(3.0f)));

       syntax.add(new WheelDriveFromFloats());
       syntax.add(new WheelDriveFromBearing());
       syntax.add(new WheelDriveSpotTurnLeft());
       syntax.add(new WheelDriveSpotTurnRight());
       syntax.add(new ScaleWheelDrive());

       syntax.add(new Literal(new ScaleFactor(0.25f)));
       syntax.add(new Literal(new ScaleFactor(0.5f)));
       syntax.add(new Literal(new ScaleFactor(0.75f)));

       syntax.add(new ReadingToDistance());
       syntax.add(new ReadingToFloat());
       syntax.add(new ReadingPresent());

       syntax.add(new RandomBearing());
       //Map<Node,Map<String,Object>> state = new HashMap<>();
       //syntax.add(new LoadBearing(state));
       //syntax.add(new SaveBearing(state));

       syntax.addAll(inputs);
       setSyntax(syntax);

       for(Node n : syntax){
           if(Literal.class.isAssignableFrom(n.getClass())) parser.declareLiteral(n.toString(), ((Literal)n).getValue());
           else if(Variable.class.isAssignableFrom(n.getClass())) parser.declareVariable((Variable)n);
           else parser.declareFunction(n.getIdentifier(), n);
       }
   }

    public List<Variable> getInputs() {
        return inputs;
    }

    public EpoxParser getParser(){
        return parser;
    }

    @Override
    public double getFitness(CandidateProgram p){
        throw new RuntimeException("Method disabled; use getGroupFitness");
    }

    public List<Double> getGroupFitness(List<GPCandidateProgram> team){
        List<Pair<List<Double>,Double>> fitnessStatses = IntStream.range(0, 3)
                .mapToObj(i -> resultForOneSim(team))
                .collect(Collectors.toList());
        //average over the runs and negate
        List<Double> fitnesses =  fitnessStatses.stream()
                .map(p -> p.getFirst())
                .reduce((a,b) -> sumLists(a,b))
                .get() //guaranteed to have 3 elements
                .stream()
                .map(d -> -d/3)
                .collect(Collectors.toList());
        Double teamFitness = fitnessStatses.stream()
                .mapToDouble(p -> -p.getSecond())
                .average()
                .getAsDouble(); //guaranteed to have 3 elements

        //update fittest team stats
        Double genMinTeamFitness =  (Double)Stats.get().getStat(CustomStatFields.GEN_TEAM_FITNESS_MIN);
        if(genMinTeamFitness == null || teamFitness < genMinTeamFitness){
            Stats.get().addData(CustomStatFields.GEN_TEAM_FITNESS_MIN, teamFitness);
            Stats.get().addData(CustomStatFields.GEN_FITTEST_TEAM, team);
        }

        Double runMinTeamFitness =  (Double)Stats.get().getStat(CustomStatFields.RUN_TEAM_FITNESS_MIN);
        if(runMinTeamFitness == null || teamFitness < runMinTeamFitness){
            Stats.get().addData(CustomStatFields.RUN_TEAM_FITNESS_MIN, teamFitness);
            Stats.get().addData(CustomStatFields.RUN_FITTEST_TEAM, team);
        }
        return fitnesses;
    }

    //returns a list of individual fitnesses and a team fitness
    protected Pair<List<Double>,Double> resultForOneSim(List<GPCandidateProgram> pl){
        config.setSimulationSeed(System.currentTimeMillis());
        List<Phenotype> phenotypes = pl.stream().map(p -> new GPPhenotype(sensors.stream().map(s -> s.clone()).collect(Collectors.toList()), p, inputs)).collect(Collectors.toList());

        HeterogeneousRobotFactory robotFactory = new HeterogeneousRobotFactory(phenotypes, config.getRobotMass(),
                config.getRobotRadius(), config.getRobotColour());
        Simulation sim = new Simulation(config, robotFactory);
        sim.run();
        //System.out.print('.');
        FitnessStats fitnesses = sim.getFitness();
        //we pass through the fitness stats, except that only we know the phenotypes, so we do the mapping here
        return new Pair<>(phenotypes.stream().map(p -> fitnesses.getPhenotypeFitness(p)).collect(Collectors.toList()), fitnesses.getTeamFitnessWithTimeBonus(sim.getStepNumber()));
    }

    public Class getReturnType(){
        return WheelDrive.class;
    }
}
