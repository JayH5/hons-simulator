package za.redbridge.simulator.sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamie on 2014/08/05.
 */
public class ProximitySensor extends Sensor {

    private final List<Double> readings = new ArrayList<>(1);

    public ProximitySensor(double bearing) {
        super(bearing, 0.0, 30.0, 0.1);
    }

    public ProximitySensor(double bearing, double orientation, double range, double fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected SensorReading provideReading(List<SensedObject> objects) {
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
