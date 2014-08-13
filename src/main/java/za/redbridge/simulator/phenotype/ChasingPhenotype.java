package za.redbridge.simulator.phenotype;

import sim.util.Double2D;
import za.redbridge.simulator.sensor.ProximitySensor;
import za.redbridge.simulator.sensor.Sensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.ArrayList;
import java.util.List;

public class ChasingPhenotype implements Phenotype {

    private final List<Sensor> sensors;

    public ChasingPhenotype() {
        ProximitySensor leftSensor = new ProximitySensor((float) ((7 / 4.0f) * Math.PI));
        ProximitySensor forwardSensor = new ProximitySensor(0.0f);
        ProximitySensor rightSensor = new ProximitySensor((float) (Math.PI/4));
        sensors = new ArrayList<>();
        sensors.add(leftSensor);
        sensors.add(forwardSensor);
        sensors.add(rightSensor);
    }

    @Override
    public List<Sensor> getSensors() {
        return sensors;
    }

    @Override
    public Double2D step(List<SensorReading> list) {
        Double2D left = new Double2D(0.0,1.0);
        Double2D forward = new Double2D(0,0);
        Double2D right = new Double2D(1.0,0);
        Double2D random = new Double2D((float)Math.random()*2f - 1f, (float)Math.random()*2f - 1f);
        double leftReading = list.get(0).getValues().get(0);
        double forwardReading = list.get(1).getValues().get(0);
        double rightReading = list.get(2).getValues().get(0);
        double max = Math.max(leftReading,Math.max(forwardReading,rightReading));
        if(max < 0.0001){
            return random;
        }else if(leftReading == max) {
            return left;
        }else if(rightReading == max) {
            return right;
        }else {
            return forward;
        }
    }

    @Override
    public Phenotype clone() {
        return new ChasingPhenotype();
    }
}
