package za.redbridge.simulator.gp;

import org.epochx.epox.Node;
import org.epochx.epox.Variable;
import org.epochx.epox.math.AddFunction;
import org.epochx.epox.math.DivisionProtectedFunction;
import org.epochx.epox.math.MultiplyFunction;
import org.epochx.epox.math.SubtractFunction;
import org.epochx.gp.model.GPModel;
import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.representation.CandidateProgram;
import za.redbridge.simulator.sensor.AgentSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xenos on 9/9/14.
 */
public class AgentModel extends GPModel {
    List<Node> inputs = new ArrayList<Node>();
   public AgentModel(List<AgentSensor> sensors){
       List<Node> syntax = new ArrayList<Node>();
       for(int i = 0; i < sensors.size(); i++){
           try {
               inputs.add(new Variable("Sensor input " + i, Class.forName("Double")));
           }catch(ClassNotFoundException e){}
       }
       syntax.add(new AddFunction());
       syntax.add(new SubtractFunction());
       syntax.add(new DivisionProtectedFunction());
       syntax.add(new MultiplyFunction());
       syntax.addAll(inputs);
       setSyntax(syntax);
   }

    @Override
    public double getFitness(CandidateProgram p){
        GPCandidateProgram program = (GPCandidateProgram) p;
        return 0.0;
    }

    public Class getReturnType(){
        try {
            return Class.forName("Double2D");
        }catch(ClassNotFoundException e){
            throw new RuntimeException("Could not find class Double2D");
        }
    }
}
