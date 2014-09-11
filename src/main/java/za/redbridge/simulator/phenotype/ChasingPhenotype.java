package za.redbridge.simulator.phenotype;

import org.jbox2d.dynamics.Fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import sim.util.Double2D;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ProximityAgentSensor;
import za.redbridge.simulator.sensor.SensorReading;

public class ChasingPhenotype implements Phenotype {
    private static final int COOLDOWN = 10;

    private int cooldownCounter = 0;
    private Double2D lastMove = null;
    private final List<AgentSensor> sensors;

    public ChasingPhenotype() {

        AgentSensor leftSensor = new ChasingSensor((float) (Math.PI / 4), 0f, 1f, 0.2f);
        AgentSensor forwardSensor = new ChasingSensor(0f, 0f, 1f, 0.2f);
        AgentSensor rightSensor = new ChasingSensor((float) -(Math.PI / 4), 0f, 1f, 0.2f);

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

        if(cooldownCounter > 0) {
            cooldownCounter--;
            return lastMove;
        }else {
            cooldownCounter = COOLDOWN;
        }

        double leftReading = list.get(0).getValues().get(0);
        double forwardReading = list.get(1).getValues().get(0);
        double rightReading = list.get(2).getValues().get(0);
        double max = Math.max(leftReading, Math.max(forwardReading, rightReading));
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

    private static class ChasingSensor extends ProximityAgentSensor {

        private static final int readingSize = 1;

        public ChasingSensor(float bearing) {
            super(bearing);
        }

        public ChasingSensor(float bearing, float orientation, float range, float fieldOfView) {
            super(bearing, orientation, range, fieldOfView);
        }

        @Override
        public boolean isRelevantObject(Fixture otherFixture) {
            return otherFixture.getBody().getUserData() instanceof ResourceObject;
        }

        @Override
        protected boolean filterOutObject(PhysicalObject object) {
            ResourceObject resourceObject = (ResourceObject) object;
            return resourceObject.isCollected();
        }

        @Override
        public int getReadingSize() { return readingSize; }
    }

    @Override
    public void configure(Map<String,Object> phenotypeConfigs) {}
}
