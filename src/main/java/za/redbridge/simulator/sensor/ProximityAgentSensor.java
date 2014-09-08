package za.redbridge.simulator.sensor;

import java.util.ArrayList;
import java.util.List;

import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

/**
 * Created by jamie on 2014/08/05.
 */
public class ProximityAgentSensor extends AgentSensor {

    private final List<Double> readings = new ArrayList<>(1);

    public ProximityAgentSensor(float bearing) {
        this(bearing, 0.0f, 0.4f, 0.1f);
    }

    public ProximityAgentSensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected SensorReading provideObjectReading(List<SensedObject> objects) {
        double reading = 0.0;
        if (!objects.isEmpty()) {
            reading = 1 - Math.min(objects.get(0).getDistance() / range, 1.0);
        }

        readings.clear();
        readings.add(reading);
        return new SensorReading(readings);
    }

    protected double readingCurve(double fraction) {
        // Sigmoid proximity response
        final double offset = 0.5;
        return 1 / (1 + Math.exp(fraction + offset));
    }
}
