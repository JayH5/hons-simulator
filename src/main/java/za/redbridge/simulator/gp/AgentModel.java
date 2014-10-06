package za.redbridge.simulator.gp;

import org.epochx.epox.EpoxParser;
import org.epochx.epox.Literal;
import org.epochx.epox.Node;
import org.epochx.epox.Variable;
import org.epochx.epox.lang.IfFunction;
import org.epochx.gp.model.GPModel;
import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.representation.CandidateProgram;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HeterogeneousRobotFactory;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.gp.functions.*;
import za.redbridge.simulator.gp.types.*;
import za.redbridge.simulator.khepera.BottomProximitySensor;
import za.redbridge.simulator.phenotype.GPPhenotype;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.TypedProximityAgentSensor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by xenos on 9/9/14.
 */
public class AgentModel extends GPModel {
    //number of simulations run per phenotype evaluation
    protected int numSims;
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
       this.numSims = 3;
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
       syntax.add(new Literal(new GPFloatLiteral(1.0f)));
       //syntax.add(new RandomGPFloat());
       //syntax.add(new Literal(new FloatLiteral(2.0f)));
       //syntax.add(new Literal(new FloatLiteral(3.0f)));

       syntax.add(new WheelDriveFromFloats());
       syntax.add(new WheelDriveFromBearing());
       syntax.add(new WheelDriveFromCoordinate());
       syntax.add(new WheelDriveSpotTurnLeft());
       syntax.add(new WheelDriveSpotTurnRight());

       syntax.add(new ReadingToCoordinate());
       syntax.add(new ReadingToDistance());
       syntax.add(new ReadingToFloat());
       syntax.add(new ReadingPresent());

       syntax.add(new RotateCoordinate());
       syntax.add(new BearingFromCoordinate());
       syntax.add(new RandomBearing());
       Map<Node,Map<String,Object>> state = new HashMap<>();
       syntax.add(new LoadBearing(state));
       syntax.add(new SaveBearing(state));

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

    public List<Double> getGroupFitness(List<GPCandidateProgram> p){
        List<List<Double>> results = IntStream.range(0, numSims)
                .parallel()
                .mapToObj(i -> resultForOneSim(p))
                .collect(Collectors.toList());
        List<Double> sums = new ArrayList<>();
        for(int i = 0; i < results.get(0).size(); i++) sums.add(0d);
        for(List<Double> run : results){
            for(int i = 0; i < run.size(); i++){
                sums.set(i, sums.get(i) + run.get(i));
            }
        }
        for(int i = 0; i < sums.size(); i++) sums.set(i, sums.get(i) / numSims);
        return sums;
    }

    protected List<Double> resultForOneSim(List<GPCandidateProgram> pl){
        config.setSimulationSeed(System.currentTimeMillis());
        List<Phenotype> phenotypes = pl.stream().map(p -> new GPPhenotype(sensors.stream().map(s -> s.clone()).collect(Collectors.toList()), p, inputs)).collect(Collectors.toList());

        HeterogeneousRobotFactory robotFactory = new HeterogeneousRobotFactory(phenotypes, config.getRobotMass(),
                config.getRobotRadius(), config.getRobotColour());
        Simulation sim = new Simulation(config, robotFactory);
        sim.run();
        System.out.print('.');
        //TODO fix! Actually track individual fitnesses in the simulation!
        double fitness = -sim.getFitness();
        List<Double> result = new ArrayList<>();
        for(int i = 0; i < pl.size(); i++)result.add(fitness);
        return result;
    }

    public Class getReturnType(){
        return WheelDrive.class;
    }
}
