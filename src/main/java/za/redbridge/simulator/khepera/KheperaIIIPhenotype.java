package za.redbridge.simulator.khepera;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.AgentSensor;

/**
 * Phenotype that mimics the morphology of a Khepera III robot.
 * Created by jamie on 2014/09/22.
 */
public abstract class KheperaIIIPhenotype implements Phenotype {

    private final List<AgentSensor> sensors;

    private final Configuration configuration;

    /**
     * Constructor used for phenotypes that make use of {@link #configure(Map)}. Sensors will not
     * be initialized (i.e. there will be no sensors) if this constructor is used until the
     * phenotype is configured.
     * TODO: Implement configure
     */
    public KheperaIIIPhenotype() {
        configuration = new Configuration();
        sensors = new ArrayList<>();
    }

    /**
     * Default constructor for this phenotype. Sensors will be configured according to
     * configuration.
     */
    public KheperaIIIPhenotype(Configuration config) {
        configuration = new Configuration(config);
        sensors = new ArrayList<>(config.getNumberOfSensors());
        
        initSensors();
    }

    private void initSensors() {
        // Proximity sensors
        if (configuration.enableProximitySensors10Degrees) {
            sensors.add(createProximitySensor((float) Math.toRadians(10), 0f));
            sensors.add(createProximitySensor((float) Math.toRadians(-10), 0f));
        }

        if (configuration.enableProximitySensors40Degrees) {
            sensors.add(createProximitySensor((float) Math.toRadians(40), 0f));
            sensors.add(createProximitySensor((float) Math.toRadians(-40), 0f));
        }

        if (configuration.enableProximitySensors75Degrees) {
            sensors.add(createProximitySensor((float) Math.toRadians(75), 0f));
            sensors.add(createProximitySensor((float) Math.toRadians(-75), 0f));
        }

        if (configuration.enableProximitySensors140Degrees) {
            sensors.add(createProximitySensor((float) Math.toRadians(140), 0f));
            sensors.add(createProximitySensor((float) Math.toRadians(-140), 0f));
        }

        if (configuration.enableProximitySensor180Degrees) {
            sensors.add(createProximitySensor((float) Math.PI, 0f));
        }

        if (configuration.enableProximitySensorBottom) {
            sensors.add(createBottomProximitySensor());
        }

        // Ultrasonic sensors
        if (configuration.enableUltrasonicSensor0Degrees) {
            sensors.add(createUltrasonicSensor(0f, 0f));
        }

        if (configuration.enableUltrasonicSensors40Degrees) {
            sensors.add(createUltrasonicSensor((float) Math.toRadians(40), 0f));
            sensors.add(createUltrasonicSensor((float) Math.toRadians(-40), 0f));
        }

        if (configuration.enableUltrasonicSensors90Degrees) {
            sensors.add(createUltrasonicSensor((float) Math.PI / 2, 0f));
            sensors.add(createUltrasonicSensor((float) -Math.PI / 2, 0f));
        }
    }

    /** Method can be overridden to customize proximity sensor */
    protected AgentSensor createProximitySensor(float bearing, float orientation) {
        return new ProximitySensor(bearing, orientation);
    }

    /** Method can be overridden to customize bottom proximity sensor */
    protected AgentSensor createBottomProximitySensor() {
        return new BottomProximitySensor();
    }

    /** Method can be overridden to customize ultrasonic sensor */
    protected AgentSensor createUltrasonicSensor(float bearing, float orientation) {
        return new UltrasonicSensor(bearing, orientation);
    }

    /** Returns a copy of the current configuration */
    public Configuration getConfiguration() {
        return new Configuration(configuration);
    }

    @Override
    public List<AgentSensor> getSensors() {
        return sensors;
    }

    @Override
    public KheperaIIIPhenotype clone() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "This is an abstract class, implement clone in your subclass");
    }

    @Override
    public void configure(Map<String, Object> phenotypeConfigs) {
        // TODO: Implementation
        //initSensors();
    }

    /**
     * Configuration for the Khepera III phenotype that allows sensors to be enabled or disabled.
     * By default all sensors are disabled.
     */
    public static class Configuration {
        public boolean enableProximitySensors10Degrees = false;
        public boolean enableProximitySensors40Degrees = false;
        public boolean enableProximitySensors75Degrees = false;
        public boolean enableProximitySensors140Degrees = false;
        public boolean enableProximitySensor180Degrees = false;

        public boolean enableProximitySensorBottom = false;
        
        public boolean enableUltrasonicSensor0Degrees = false;
        public boolean enableUltrasonicSensors40Degrees = false;
        public boolean enableUltrasonicSensors90Degrees = false;

        public Configuration() {
        }

        private Configuration(Configuration other) {
            this.enableProximitySensors10Degrees = other.enableProximitySensors10Degrees;
            this.enableProximitySensors40Degrees = other.enableProximitySensors40Degrees;
            this.enableProximitySensors75Degrees = other.enableProximitySensors75Degrees;
            this.enableProximitySensors140Degrees = other.enableProximitySensors140Degrees;
            this.enableProximitySensor180Degrees = other.enableProximitySensor180Degrees;

            this.enableProximitySensorBottom = other.enableProximitySensorBottom;

            this.enableUltrasonicSensor0Degrees = other.enableUltrasonicSensor0Degrees;
            this.enableUltrasonicSensors40Degrees = other.enableUltrasonicSensors40Degrees;
            this.enableUltrasonicSensors90Degrees = other.enableUltrasonicSensors90Degrees;
        }
        
        public int getNumberOfSensors() {
            int numSensors = 0;
            
            if (enableProximitySensors10Degrees) numSensors += 2;
            if (enableProximitySensors40Degrees) numSensors += 2;
            if (enableProximitySensors75Degrees) numSensors += 2;
            if (enableProximitySensors140Degrees) numSensors += 2;
            if (enableProximitySensor180Degrees) numSensors += 1;
            if (enableProximitySensorBottom) numSensors += 1;
            if (enableUltrasonicSensor0Degrees) numSensors += 1;
            if (enableUltrasonicSensors40Degrees) numSensors += 2;
            if (enableUltrasonicSensors90Degrees) numSensors += 2;
            
            return numSensors;
        }
    }

}
