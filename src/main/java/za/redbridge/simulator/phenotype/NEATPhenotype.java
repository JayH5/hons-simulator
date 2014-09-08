package za.redbridge.simulator.phenotype;

import sim.util.Double2D;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.List;

/**
 * Created by shsu on 2014/09/08.
 */
public class NEATPhenotype implements Phenotype {

    private final List<AgentSensor> sensors;


    public NEATPhenotype(List<AgentSensor> sensors) {
        this.sensors = sensors;
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

}
