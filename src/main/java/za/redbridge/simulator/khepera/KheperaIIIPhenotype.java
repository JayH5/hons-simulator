package za.redbridge.simulator.khepera;

import java.util.ArrayList;
import java.util.List;

import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.AgentSensor;

/**
 * Phenotype that mimics the morphology of a Khepera III robot.
 * Created by jamie on 2014/09/22.
 */
public abstract class KheperaIIIPhenotype implements Phenotype {

    private static final boolean ENABLE_PROXIMITY_SENSORS_10_DEGREES = true;
    private static final boolean ENABLE_PROXIMITY_SENSORS_40_DEGREES = false;
    private static final boolean ENABLE_PROXIMITY_SENSORS_75_DEGREES = true;
    private static final boolean ENABLE_PROXIMITY_SENSORS_140_DEGREES = false;
    private static final boolean ENABLE_PROXIMITY_SENSOR_180_DEGREES = true;

    private static final boolean ENABLE_ULTRASONIC_SENSOR_0_DEGREES = true;
    private static final boolean ENABLE_ULTRASONIC_SENSORS_40_DEGREES = false;
    private static final boolean ENABLE_ULTRASONIC_SENSORS_90_DEGREES = true;

    public static final int NUM_SENSORS;
    static {
        int numSensors = 0;
        if (ENABLE_PROXIMITY_SENSORS_10_DEGREES) numSensors += 2;
        if (ENABLE_PROXIMITY_SENSORS_40_DEGREES) numSensors += 2;
        if (ENABLE_PROXIMITY_SENSORS_75_DEGREES) numSensors += 2;
        if (ENABLE_PROXIMITY_SENSORS_140_DEGREES) numSensors += 2;
        if (ENABLE_PROXIMITY_SENSOR_180_DEGREES) numSensors += 1;
        if (ENABLE_ULTRASONIC_SENSOR_0_DEGREES) numSensors += 1;
        if (ENABLE_ULTRASONIC_SENSORS_40_DEGREES) numSensors += 2;
        if (ENABLE_ULTRASONIC_SENSORS_90_DEGREES) numSensors += 2;
        NUM_SENSORS = numSensors;
    }

    private final List<AgentSensor> sensors = new ArrayList<>(NUM_SENSORS);

    public KheperaIIIPhenotype() {
        initSensors();
    }

    private void initSensors() {
        // Proximity sensors
        if (ENABLE_PROXIMITY_SENSORS_10_DEGREES) {
            sensors.add(new ProximitySensor((float) Math.toRadians(10), 0f));
            sensors.add(new ProximitySensor((float) Math.toRadians(-10), 0f));
        }

        if (ENABLE_PROXIMITY_SENSORS_40_DEGREES) {
            sensors.add(new ProximitySensor((float) Math.toRadians(40), 0f));
            sensors.add(new ProximitySensor((float) Math.toRadians(-40), 0f));
        }

        if (ENABLE_PROXIMITY_SENSORS_75_DEGREES) {
            sensors.add(new ProximitySensor((float) Math.toRadians(75), 0f));
            sensors.add(new ProximitySensor((float) Math.toRadians(-75), 0f));
        }

        if (ENABLE_PROXIMITY_SENSORS_140_DEGREES) {
            sensors.add(new ProximitySensor((float) Math.toRadians(140), 0f));
            sensors.add(new ProximitySensor((float) Math.toRadians(-140), 0f));
        }

        if (ENABLE_PROXIMITY_SENSOR_180_DEGREES) {
            sensors.add(new ProximitySensor((float) Math.PI, 0f));
        }

        // Ultrasonic sensors
        if (ENABLE_ULTRASONIC_SENSOR_0_DEGREES) {
            sensors.add(new UltrasonicSensor(0f, 0f));
        }

        if (ENABLE_ULTRASONIC_SENSORS_40_DEGREES) {
            sensors.add(new UltrasonicSensor((float) Math.toRadians(40), 0f));
            sensors.add(new UltrasonicSensor((float) Math.toRadians(-40), 0f));
        }

        if (ENABLE_ULTRASONIC_SENSORS_90_DEGREES) {
            sensors.add(new UltrasonicSensor((float) Math.PI / 2, 0f));
            sensors.add(new UltrasonicSensor((float) -Math.PI / 2, 0f));
        }
    }

    @Override
    public List<AgentSensor> getSensors() {
        return sensors;
    }

    @Override
    public KheperaIIIPhenotype clone() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
