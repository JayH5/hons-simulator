package za.redbridge.simulator.phenotype;

import org.epochx.epox.Variable;
import org.epochx.gp.representation.GPCandidateProgram;
import sim.util.Double2D;
import za.redbridge.simulator.gp.types.WheelDrive;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.TypedProximityAgentSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Double2D step(List<List<Double>> list) {
        if(list.size() != sortedInputs.size()) throw new IllegalArgumentException("SensorReading list needs to be of size " + sortedInputs.size());
        for(int i = 0; i < list.size(); i++){
            AgentSensor sensor = sensors.get(i);
            //if it's typed, pass in entire list; it will have one reading per agent type. Otherwise, pass in the first and only value
            if(sensor instanceof TypedProximityAgentSensor) sortedInputs.get(i).setValue(list.get(i).stream().map(v -> v.floatValue()).collect(Collectors.toList()));
            else sortedInputs.get(i).setValue(list.get(i).get(0).floatValue());
        }
        WheelDrive d = (WheelDrive) program.evaluate();
        return new Double2D(d.x, d.y);
    }

    @Override
    public Phenotype clone() {
        List<AgentSensor> newSensors = new ArrayList<>();
        for(AgentSensor s : sensors){
            newSensors.add((AgentSensor)s.clone());
        }
        return new GPPhenotype(newSensors, program.clone(), sortedInputs);
    }

    @Override
    public void configure(Map<String,Object> config){}
}
