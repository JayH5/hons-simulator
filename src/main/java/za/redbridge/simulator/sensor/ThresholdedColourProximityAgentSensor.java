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
        readings.add(reading);
        return new SensorReading(readings);
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException {

        String sensitive = null;
        additionalConfigs = map;

        if (map == null) {
            System.out.println("No additional configs found.");
            return;
        }

        if (checkFieldPresent(map, "sensitiveClass")) {
            sensitive = (String) map.get(sensitiveClass);

                try {
                    sensitiveClass = Class.forName(sensitive);
                }
                catch (ClassNotFoundException c) {
                    System.out.println("Specified sensitive class not found.");
                    System.exit(-1);
                }

        }
        else {
            throw new ParseException("No additional configs found for ThresholdedObjectProximityAgentSensor.", 0);
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
}
