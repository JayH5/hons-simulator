package za.redbridge.simulator.phenotype;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import sim.util.Double2D;
import za.redbridge.simulator.sensor.AgentSensor;

/**
 * Interface to the agent.
 */
public interface Phenotype extends Cloneable {

    /**
     * Returns the list of sensors this bot has on it
     * @return a List of AgentSensor objects
     */
    List<AgentSensor> getSensors();

    /**
     * Process the sensor inputs and provide actuator outputs
     * @param list the current environment state
     * @return The vector for driving the actuators
     */
    Double2D step(List<List<Double>> list);

    Phenotype clone();

    void configure(Map<String,Object> phenotypeConfigs);

    /**
     * Represents a controller that outputs a one vector. For testing.
     */
    public static final Phenotype DUMMY_PHENOTYPE = new Phenotype() {
        private final Double2D one = new Double2D(1.0, 1.0);

        @Override
        public Double2D step(List<List<Double>> l) {
            return one;
        }

        @Override
        public Phenotype clone() {
            return this;
        }

        @Override
        public List<AgentSensor> getSensors() {
            return Collections.emptyList();
        }

        @Override
        public void configure(Map<String,Object> phenotypeConfigs) {}

    };

}
