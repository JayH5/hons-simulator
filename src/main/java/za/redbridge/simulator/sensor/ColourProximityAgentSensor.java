package za.redbridge.simulator.sensor;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.WallObject;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

public class ColourProximityAgentSensor extends AgentSensor {

    private final List<Double> readings = new ArrayList<>(3);

    public ColourProximityAgentSensor(float bearing) {
        super(bearing, 0.0f, 30.0f, 0.1f);
        readings.add(0.0);
        readings.add(0.0);
        readings.add(0.0);
    }

    public ColourProximityAgentSensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected SensorReading provideObjectReading(List<SensedObject> objects) {
        if (!objects.isEmpty()) {
            SensedObject closest = objects.get(0);
            double reading = 1 - Math.min(closest.getDistance() / range, 1.0);
            if(closest.getObject() instanceof RobotObject) readings.set(0, reading);
            else readings.set(0, 0.0);
            if(closest.getObject() instanceof ResourceObject) readings.set(1, reading);
            else readings.set(1, 0.0);
            if(closest.getObject() instanceof WallObject) readings.set(2, reading);
            else readings.set(2, 0.0);
        } else {
            readings.set(0, 0.0);
            readings.set(1, 0.0);
            readings.set(2, 0.0);
        }

        return new SensorReading(readings);
    }

    @Override
    protected Paint getPaint() {
        return new Color(readings.get(0).floatValue(), readings.get(1).floatValue(),
                readings.get(2).floatValue(), 0.5f);
    }

    protected double readingCurve(double fraction) {
        // Sigmoid proximity response
        final double offset = 0.5;
        return 1 / (1 + Math.exp(fraction + offset));
    }
}
