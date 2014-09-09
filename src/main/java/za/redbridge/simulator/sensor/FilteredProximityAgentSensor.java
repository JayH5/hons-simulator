package za.redbridge.simulator.sensor;

import java.text.ParseException;
import java.util.*;

import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

public class FilteredProximityAgentSensor extends AgentSensor {

    private final List<Double> readings = new ArrayList<>(1);
    private Set<Class> whitelist = null;

    public FilteredProximityAgentSensor() {
        super();
    }

    public FilteredProximityAgentSensor(float bearing, Collection<String> whitelist) {
        super(bearing, 0.0f, 30.0f, 0.1f);
        for(String cs : whitelist){
            try {
                this.whitelist.add(Class.forName(cs));
            }catch(ClassNotFoundException e){}
        }
    }

    public FilteredProximityAgentSensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected SensorReading provideObjectReading(List<SensedObject> objects) {
        for(SensedObject o : objects){
            if(!whitelist.contains(o.getObject().getClass())){
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

    protected double readingCurve(double fraction) {
        // Sigmoid proximity response
        final double offset = 0.5;
        return 1 / (1 + Math.exp(fraction + offset));
    }

    public void setWhitelist(Collection<String> whitelist){

        for(String cs : whitelist){
            try {
                this.whitelist.add(Class.forName(cs));
            }catch(ClassNotFoundException e){}
        }
    }

    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException {

        String[] whiteList = null;

        if (checkFieldPresent(map, "whiteList")) {
            whiteList = ((String) map.get("whiteList")).split(" ");

            for (String cs: whiteList) {

                try {
                    whitelist.add(Class.forName(cs));
                }
                catch (ClassNotFoundException c) {
                    System.out.println("Specified whitelist class not found.");
                    System.exit(-1);
                }
            }
        }
        else {
            throw new ParseException("No whitelist found for FilteredProximitySensor configs.", 0);
        }

    }
}
