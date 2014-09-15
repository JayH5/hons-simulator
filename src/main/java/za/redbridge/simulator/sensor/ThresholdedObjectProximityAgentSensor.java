package za.redbridge.simulator.sensor;

import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/09/15.
 */
//Object types are analogous to colours
public class ThresholdedObjectProximityAgentSensor extends AgentSensor {

    protected final List<Double> readings = new ArrayList<>(1);
    protected static final int readingSize = 1;
    protected double sensitivity;

    protected Class sensitiveClass;

    public ThresholdedObjectProximityAgentSensor() {
        super();

        sensitivity = 0;
        try {
            sensitiveClass = Class.forName("za.redbridge.simulator.object.ResourceObject");
        }
        catch (ClassNotFoundException c) {
            System.out.println("Class not found.");
        }
    }

    public ThresholdedObjectProximityAgentSensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    public ThresholdedObjectProximityAgentSensor(float bearing, String sensitiveClassName) {
        super(bearing, 0.0f, 30.0f, 0.1f);

            try {
                this.sensitiveClass = Class.forName(sensitiveClassName);
            }catch(ClassNotFoundException e){}

    }

    @Override
    protected SensorReading provideObjectReading(List<SensedObject> objects) {
        for(SensedObject o : objects){
            if(!(o.getObject().getClass().equals(sensitiveClass))){
                objects.remove(o);
            }
        }
        double reading = 0.0;
        if (!objects.isEmpty()) {
            reading = 1 - Math.min(objects.get(0).getDistance() / range, 1.0);
        }

        readings.clear();

        //threshold
        if (reading >= (1-sensitivity)) {
            readings.add(reading);
        }
        else {
            readings.add(0.0);
        }

        return new SensorReading(readings);
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException {

        additionalConfigs = map;

        if (map == null) {
            System.out.println("No additional configs found.");
            return;
        }

        String sensitive = (String) map.get("sensitiveClass");

        if (checkFieldPresent(sensitive, "sensitiveClass")) {
                try {
                    sensitiveClass = Class.forName(sensitive);
                }
                catch (ClassNotFoundException c) {
                    System.out.println("Specified sensitive class not found.");
                    System.exit(-1);
                }
        }
        else {
            throw new ParseException("No sensitive class found for ThresholdedObjectProximityAgentSensor.", 0);
        }

        Number sens = (Number) map.get("sensitivity");
        if (checkFieldPresent(sens, "sensitivity")) {
            double sensValue = sens.doubleValue();

            if (sensValue > 1 || sensValue < 0) {
                throw new ParseException("Sensitivity value for ThresholdedObjectProximityAgentSensor must be between 0 and 1", 0);
            }

            this.sensitivity = sensValue;
        }
        else {
            throw new ParseException("No sensitivity value found for ThresholdedObjectProximityAgentSensor.", 0);
        }

    }

    @Override
    public int getReadingSize() { return readingSize; }

    @Override
    public ThresholdedObjectProximityAgentSensor clone() {

        ThresholdedObjectProximityAgentSensor cloned =
                new ThresholdedObjectProximityAgentSensor(bearing, orientation, range, fieldOfView);

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

    public void setSensitivity(double sensitivity) { this.sensitivity = sensitivity; }

    @Override
    public Map<String,Object> getAdditionalConfigs() { return additionalConfigs; }
}
