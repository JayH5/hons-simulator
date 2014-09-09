package za.redbridge.simulator.phenotype;

import org.encog.neural.neat.NEATNetwork;
import sim.util.Double2D;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/09/08.
 */
public class NEATPhenotype implements Phenotype {

    private final List<AgentSensor> sensors;
    private final NEATNetwork controller;

    public NEATPhenotype(List<AgentSensor> sensors, NEATNetwork controller) {

        this.sensors = sensors;
        this.controller = controller;
    }

    @Override
    public List<AgentSensor> getSensors () {
        return sensors;
    }

    @Override
    public Double2D step(List<SensorReading> list) {

        return new Double2D(0,0);
    }

    public NEATPhenotype clone() {
        return this;
    }

    public NEATNetwork getController() { return controller; }

    public void configure(Map<String,Object> phenotypeConfigs) {}

}
