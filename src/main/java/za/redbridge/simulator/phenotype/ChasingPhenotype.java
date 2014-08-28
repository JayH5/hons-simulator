package za.redbridge.simulator.phenotype;

import sim.util.Double2D;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ProximityAgentSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.ArrayList;
import java.util.List;

public class ChasingPhenotype implements Phenotype {
    private int cooldown = 10;
    private int cooldownCounter = 0;
    private Double2D lastMove = null;
    private final List<AgentSensor> sensors;

    public ChasingPhenotype() {
        ProximityAgentSensor leftSensor = new ProximityAgentSensor((float) ((7 / 4.0f) * Math.PI));
        ProximityAgentSensor forwardSensor = new ProximityAgentSensor(0.0f);
        ProximityAgentSensor rightSensor = new ProximityAgentSensor((float) (Math.PI/4));
        sensors = new ArrayList<>();
        sensors.add(leftSensor);
        sensors.add(forwardSensor);
        sensors.add(rightSensor);
    }

    @Override
    public List<AgentSensor> getSensors() {
        return sensors;
    }

    @Override
    public Double2D step(List<SensorReading> list) {
        Double2D left = new Double2D(0.5,1.0);
        Double2D forward = new Double2D(1.0,1.0);
        Double2D right = new Double2D(1.0,0.5);
        Double2D random = new Double2D((float)Math.random()*2f - 1f, (float)Math.random()*2f - 1f);
        Double2D nothing = new Double2D(0,0);

        if(cooldownCounter > 0) {
            cooldownCounter--;
            return lastMove;
        }else {
            cooldownCounter = cooldown;
        }

        double leftReading = list.get(0).getValues().get(0);
        double forwardReading = list.get(1).getValues().get(0);
        double rightReading = list.get(2).getValues().get(0);
        double max = Math.max(leftReading,Math.max(forwardReading,rightReading));
        if(max < 0.0001){
            lastMove = random;
            return random;
        }else if(leftReading == max) {
            lastMove = left;
            return left;
        }else if(rightReading == max) {
            lastMove = right;
            return right;
        }else {
            lastMove = forward;
            return forward;
        }
    }

    @Override
    public Phenotype clone() {
        return new ChasingPhenotype();
    }
}
