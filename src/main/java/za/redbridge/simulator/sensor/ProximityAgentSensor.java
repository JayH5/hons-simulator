package za.redbridge.simulator.sensor;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

/**
 * Created by jamie on 2014/08/05.
 */
public class ProximityAgentSensor extends AgentSensor {

    private static final int readingSize = 1;

    public ProximityAgentSensor(float bearing) {
        this(bearing, 0.0f, 0.4f, 0.1f);
    }

    public ProximityAgentSensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected void provideObjectReading(List<SensedObject> objects, List<Double> output) {
        double reading = 0.0;
        if (!objects.isEmpty()) {
            reading = 1 - Math.min(objects.get(0).getDistance() / range, 1.0);
        }

        output.add(reading);
    }

    protected double readingCurve(double fraction) {
        // Sigmoid proximity response
        final double offset = 0.5;
        return 1 / (1 + Math.exp(fraction + offset));
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException { additionalConfigs = map; }

    @Override
    public int getReadingSize() { return readingSize; }

    @Override
    public ProximityAgentSensor clone() {

        ProximityAgentSensor cloned = new ProximityAgentSensor(bearing, orientation, range, fieldOfView);

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
