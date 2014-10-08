package za.redbridge.simulator.phenotype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sim.util.Double2D;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ProximityAgentSensor;

/**
 * Created by jamie on 2014/08/05.
 */
public class SimplePhenotype implements Phenotype {

    private final List<AgentSensor> sensors;

    public SimplePhenotype() {
        AgentSensor sensor1 = new ProximityAgentSensor(0.0f, 0.0f, 3.0f, 0.1f);
        sensors = new ArrayList<>();
        sensors.add(sensor1);
    }

    @Override
    public List<AgentSensor> getSensors() {
        return sensors;
    }

    @Override
    public Double2D step(List<List<Double>> list) {
        return new Double2D(1.0f, 0.2f);
    }

    @Override
    public SimplePhenotype clone() {
        return new SimplePhenotype();
    }

    @Override
    public void configure(Map<String,Object> phenotypeConfigs) {}
}
