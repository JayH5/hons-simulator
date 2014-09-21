package za.redbridge.simulator.gp;

import org.epochx.epox.EpoxParser;
import org.epochx.epox.Literal;
import org.epochx.epox.Node;
import org.epochx.epox.Variable;
import org.epochx.epox.lang.IfFunction;
import org.epochx.epox.math.*;
import org.epochx.gp.model.GPModel;
import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.representation.CandidateProgram;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.HomogeneousRobotFactory;
import za.redbridge.simulator.gp.functions.*;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.ProximityReading;
import za.redbridge.simulator.gp.types.WheelDrive;
import za.redbridge.simulator.phenotype.GPPhenotype;
import za.redbridge.simulator.sensor.AgentSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xenos on 9/9/14.
 */
public class AgentModel extends GPModel {
    //number of simulations run per phenotype evaluation
    public static int numSims;
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
           inputs.add(new Variable("S" + i, ProximityReading.class));
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

       for(AgentSensor s : sensors){
           syntax.add(new Literal(new Bearing(s.getOrientation())));
       }
       syntax.add(new ReadingToDistance(0.4f)); //sensor range

       syntax.add(new IfFunction());
       syntax.add(new GreaterThanFunction());

       syntax.add(new Literal(new Bearing(0.0f)));
       syntax.add(new Literal(new Bearing(P2)));
       syntax.add(new Literal(new Bearing(2*P2)));
       syntax.add(new Literal(new Bearing(3*P2)));

       syntax.add(new Literal(0.0f));
       syntax.add(new Literal(1.0f));
       //syntax.add(new Literal(2.0));
       //syntax.add(new Literal(3.0));

       syntax.add(new WheelDriveFromFloats());
       syntax.add(new WheelDriveFromBearing());
       syntax.add(new WheelDriveFromCoordinate());
       syntax.add(new CoordinateFromDistanceAndBearing());
       syntax.add(new RotateCoordinate());
       syntax.add(new BearingFromCoordinate());

       syntax.addAll(inputs);
       setSyntax(syntax);

       for(Node n : syntax){
           if(n.getClass() == Literal.class) parser.declareLiteral(n.toString(), ((Literal)n).getValue());
           else if(n.getClass() == Variable.class) parser.declareVariable((Variable)n);
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
        List<Double>results = new ArrayList<>(numSims);
        for(int i = 0; i < numSims; i++){
            results.add(resultForOneSim(p));
        }
        double total = 0.0;
        for(Double d : results){
            total += d;
        }
        return total/ numSims;
    }

    protected double resultForOneSim(CandidateProgram p){
        config.setSimulationSeed(System.currentTimeMillis());
        GPCandidateProgram program = (GPCandidateProgram) p;

        HomogeneousRobotFactory robotFactory = new HomogeneousRobotFactory(
                new GPPhenotype(sensors, program, inputs), config.getRobotMass(),
                config.getRobotRadius(), config.getRobotColour(), exConfig.getPopulationSize());
        Simulation sim = new Simulation(config, robotFactory);
        sim.runForNIterations(20000);
        System.out.print('.');
        return -sim.getFitness();
    }

    public Class getReturnType(){
        return WheelDrive.class;
    }
}
