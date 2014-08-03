package za.redbridge.simulator.sensor;

import java.util.Collections;
import java.util.List;

/**
 * A sensor reading is a list of values of the output of a single sensor
 */
public final class SensorReading {
    private final List<Double> values;

    public SensorReading(List<Double> values) {
        this.values = Collections.unmodifiableList(values);
    }

    public List<Double> getValues() {
        return values;
    }
}
