package za.redbridge.simulator.khepera;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

/**
 * A rough estimation of the ultrasonic sensor used in the Khepera III robot: the Midas 400ST/R100
 * Ultrasonic sensors are characterised by a large range but with poor tracking of objects that
 * are close to the agent. They also have a relatively wide Field of View.
 * Created by jamie on 2014/09/23.
 */
public class UltrasonicSensor extends AgentSensor {

    private static final float ULTRASONIC_SENSOR_MAX_RANGE = 4.0f; // 4 meters
    private static final float ULTRASONIC_SENSOR_MIN_RANGE = 0.2f; // 20 centimeters
    private static final float ULTRASONIC_SENSOR_FOV = 1.22f; // 35 degrees

    public UltrasonicSensor(float bearing, float orientation) {
        this(bearing, orientation, ULTRASONIC_SENSOR_MAX_RANGE, ULTRASONIC_SENSOR_FOV);
    }

    public UltrasonicSensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected void provideObjectReading(List<SensedObject> sensedObjects, List<Double> output) {
        if (!sensedObjects.isEmpty()) {
            SensedObject closestObject = sensedObjects.get(0);
            float closestDistance = closestObject.getDistance();
            if (closestDistance > ULTRASONIC_SENSOR_MIN_RANGE) {
                float range = ULTRASONIC_SENSOR_MAX_RANGE - ULTRASONIC_SENSOR_MIN_RANGE;
                float distance = closestDistance - ULTRASONIC_SENSOR_MIN_RANGE;
                output.add(1.0 - distance / range);
            } else {
                // Objects closer than the minimum range just return 1.0
                output.add(1.0);
            }
        } else {
            output.add(0.0);
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

    @Override
    public boolean isRelevantObject(PhysicalObject object) {
        return !(object instanceof TargetAreaObject);
    }
}
