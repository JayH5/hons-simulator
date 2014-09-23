package za.redbridge.simulator.sensor;

import java.awt.Color;
import java.awt.Paint;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

public class ColourProximityAgentSensor extends AgentSensor {

    private static final int readingSize = 1;

    public ColourProximityAgentSensor(float bearing) {
        this(bearing, 0.0f, 30.0f, 0.1f);
    }

    public ColourProximityAgentSensor(float bearing, float orientation, float range,
            float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected void provideObjectReading(List<SensedObject> objects, List<Double> output) {
        output.add(0.0);
        output.add(0.0);
        output.add(0.0);

        if (!objects.isEmpty()) {
            SensedObject closest = objects.get(0);
            double reading = 1 - Math.min(closest.getDistance() / range, 1.0);
            if(closest.getObject() instanceof RobotObject) output.set(0, reading);
            else output.set(0, 0.0);
            if(closest.getObject() instanceof ResourceObject) output.set(1, reading);
            else output.set(1, 0.0);
            if(closest.getObject() instanceof WallObject) output.set(2, reading);
            else output.set(2, 0.0);
        }
    }

    @Override
    protected Paint getPaint() {
        List<Double> readings = getPreviousReadings();
        if (readings.size() < 3) {
            return super.getPaint();
        }
        return new Color(readings.get(0).floatValue(), readings.get(1).floatValue(),
                readings.get(2).floatValue(), 0.5f);
    }

    protected double readingCurve(double fraction) {
        // Sigmoid proximity response
        final double offset = 0.5;
        return 1 / (1 + Math.exp(fraction + offset));
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException {
        additionalConfigs = map;
    }

    @Override
    public int getReadingSize() { return readingSize; }

    @Override
    public ColourProximityAgentSensor clone() {

        ColourProximityAgentSensor cloned =
                new ColourProximityAgentSensor(bearing, orientation, range, fieldOfView);

        try {
            cloned.readAdditionalConfigs(additionalConfigs);
        }
        catch (ParseException p) {
            System.out.println("Clone failed.");
            p.printStackTrace();
            System.exit(-1);
        }

        return cloned;
    }

    @Override
    public Map<String,Object> getAdditionalConfigs() { return additionalConfigs; }
}
