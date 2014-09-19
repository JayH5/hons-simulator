package za.redbridge.simulator.phenotype;

import org.jbox2d.dynamics.Fixture;
import sim.util.Double2D;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.ProximityAgentSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TenPointFourPhenotype implements Phenotype {
    private static final int COOLDOWN = 10;

    private int cooldownCounter = 0;
    private Double2D lastMove = null;
    private final List<AgentSensor> sensors;

    public TenPointFourPhenotype() {

        AgentSensor leftSensor = new ProximityAgentSensor((float) (Math.PI / 4), 0f, 1f, 0.2f);
        AgentSensor forwardSensor = new ProximityAgentSensor(0f, 0f, 1f, 0.2f);
        AgentSensor rightSensor = new ProximityAgentSensor((float) -(Math.PI / 4), 0f, 1f, 0.2f);

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
        return new Double2D(-1,-1);
    }

    @Override
    public Phenotype clone() {
        return new TenPointFourPhenotype();
    }

    @Override
    public void configure(Map<String,Object> phenotypeConfigs) {}
}
