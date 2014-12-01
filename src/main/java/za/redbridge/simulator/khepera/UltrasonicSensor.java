package za.redbridge.simulator.khepera;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

/**
 * A rough estimation of the ultrasonic sensor used in the Khepera III robot: the Midas 400ST/R100
 * Ultrasonic sensors are characterised by a large range but with poor tracking of objects that
 * are close to the agent. They also have a relatively wide Field of View.
 * Created by jamie on 2014/09/23.
 */
public class UltrasonicSensor extends AgentSensor {

    public static final float RANGE = 4.0f; // 4 meters
    public static final float RANGE_NO_DISTANCE = 0.2f; // 20 centimeters
    public static final float FIELD_OF_VIEW = 1.22f; // 35 degrees

    public UltrasonicSensor(float bearing, float orientation) {
        this(bearing, orientation, RANGE, FIELD_OF_VIEW);
    }

    public UltrasonicSensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected void provideObjectReading(List<SensedObject> sensedObjects, List<Double> output) {
        double reading = 0;
        if (!sensedObjects.isEmpty()) {
            SensedObject closestObject = sensedObjects.get(0);
            reading = readingCurve(closestObject.getDistance());
        }
        output.add(reading);
    }

    private double readingCurve(float distance) {
        // Normalize the distance to the standard range
        float normalizedDistance = RANGE / range * distance;
        if (normalizedDistance > RANGE_NO_DISTANCE) {
            // Find fraction of range with distance measurement
            final float rangeWithDistance = RANGE - RANGE_NO_DISTANCE;
            float measuredDistance = normalizedDistance - RANGE_NO_DISTANCE;
            return 1.0 - measuredDistance / rangeWithDistance;
        } else {
            return 1.0;
        }
    }

    @Override
    public AgentSensor clone() {
        return new UltrasonicSensor(bearing, orientation, range, fieldOfView);
    }

    @Override
    public int getReadingSize() {
        return 1;
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> stringObjectMap) throws ParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getAdditionalConfigs() {
        return null;
    }

}
