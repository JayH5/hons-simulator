package za.redbridge.simulator.phenotype;

import java.util.ArrayList;
import java.util.List;

import sim.util.Double2D;
import za.redbridge.simulator.sensor.ProximitySensor;
import za.redbridge.simulator.sensor.Sensor;
import za.redbridge.simulator.sensor.SensorReading;

/**
 * Created by jamie on 2014/08/05.
 */
public class SimplePhenotype implements Phenotype {

    private final List<Sensor> sensors;

    public SimplePhenotype() {
        ProximitySensor sensor1 = new ProximitySensor(0.0);
        sensors = new ArrayList<>();
        sensors.add(sensor1);
    }

    @Override
    public List<Sensor> getSensors() {
        return sensors;
    }

    @Override
    public Double2D step(List<SensorReading> list) {
        return new Double2D();
    }
}