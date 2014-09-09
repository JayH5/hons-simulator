package za.redbridge.simulator.phenotype;

import org.epochx.epox.Node;
import org.epochx.epox.Variable;
import org.epochx.gp.representation.GPCandidateProgram;
import sim.util.Double2D;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.ArrayList;
import java.util.List;

public class GPPhenotype implements Phenotype {
    protected List<Variable> sortedInputs;
    protected ArrayList<AgentSensor> sensors = new ArrayList<>();
    protected GPCandidateProgram program;

    public GPPhenotype(List<AgentSensor> sensors, GPCandidateProgram program, List<Variable> sortedInputs) {
        this.sensors = new ArrayList<>(sensors);
        this.program = program;
        this.sortedInputs = sortedInputs;
    }

    @Override
    public List<AgentSensor> getSensors() {
        return sensors;
    }

    @Override
    public Double2D step(List<SensorReading> list) {
        if(list.size() != sortedInputs.size()) throw new IllegalArgumentException("SensorReading list needs to be of size " + sortedInputs.size());
        for(int i = 0; i < list.size(); i++){
            sortedInputs.get(i).setValue(list.get(i).getValues().get(0));
        }
        return (Double2D) program.evaluate();
    }

    @Override
    public Phenotype clone() {
        List<Variable> newVars = new ArrayList<Variable>();
        for(Variable v: sortedInputs){
            newVars.add(new Variable(v.getIdentifier(), v.getReturnType()));
        }
        return new GPPhenotype((List<AgentSensor>)sensors.clone(), program.clone(), newVars);
    }
}
