package za.redbridge.simulator.sensor;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.WallObject;

import java.util.ArrayList;
import java.util.List;

public class ColourProximitySensor extends Sensor {

    private final List<Double> readings = new ArrayList<>(3);

    public ColourProximitySensor(float bearing) {
        super(bearing, 0.0f, 30.0f, 0.1f);
    }

    public ColourProximitySensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected SensorReading provideReading(List<SensedObject> objects) {
        readings.clear();
        if (!objects.isEmpty()) {
            SensedObject closest = objects.get(0);
            double reading = 1 - Math.min(closest.getDistance() / range, 1.0);
            if(closest.getObject() instanceof RobotObject) readings.add(reading);
            else readings.add(0.0);
            if(closest.getObject() instanceof ResourceObject) readings.add(reading);
            else readings.add(0.0);
            if(closest.getObject() instanceof WallObject) readings.add(reading);
            else readings.add(0.0);
        } else {
            readings.add(0.0);
            readings.add(0.0);
            readings.add(0.0);
        }

        return new SensorReading(readings);
    }

    protected double readingCurve(double fraction) {
        // Sigmoid proximity response
        final double offset = 0.5;
        return 1 / (1 + Math.exp(fraction + offset));
    }
}
