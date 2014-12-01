package za.redbridge.simulator.khepera;

import org.apache.commons.math3.distribution.GammaDistribution;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

/**
 * A rough estimation of the ultrasonic sensor used in the Khepera III robot: the Vishay TCRT5000
 * Created by jamie on 2014/09/23.
 */
public class ProximitySensor extends AgentSensor {

    public static final float RANGE = 0.2f;
    public static final float FIELD_OF_VIEW = 0.2f; // This is a guess

    private final GammaDistribution function = new GammaDistribution(2.5, 2.0);

    public ProximitySensor(float bearing, float orientation) {
        this(bearing, orientation, RANGE, FIELD_OF_VIEW);
    }

    public ProximitySensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected void provideObjectReading(List<SensedObject> sensedObjects, List<Double> output) {
        if (!sensedObjects.isEmpty()) {
            output.add(readingCurve(sensedObjects.get(0).getDistance()));
        } else {
            output.add(0.0);
        }
    }

    @Override
    public AgentSensor clone() {
        return new ProximitySensor(bearing, orientation, range, fieldOfView);
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

    protected double readingCurve(float distance) {
        float normalizedDistance = RANGE / range * distance;

        // Output curve of the TCRT5000 seems to produce something like a Gamma distribution curve
        // See the datasheet for more information
        return Math.min(function.density(normalizedDistance * 1000) * 6.64, 1.0);
    }

}
