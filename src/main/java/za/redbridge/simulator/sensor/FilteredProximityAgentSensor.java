package za.redbridge.simulator.sensor;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

public class FilteredProximityAgentSensor extends AgentSensor {

    private Set<Class> whitelist = new HashSet<>();
    private static final int readingSize = 1;

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
    protected void provideObjectReading(List<SensedObject> objects, List<Double> output) {
        for(SensedObject o : objects){
            if(!whitelist.contains(o.getObject().getClass())){
                objects.remove(o);
            }
        }
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

    public void setWhitelist(Collection<String> whitelist){

        for(String cs : whitelist){
            try {
                this.whitelist.add(Class.forName(cs));
            }catch(ClassNotFoundException e){}
        }
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException {

        String[] whiteList = null;
        additionalConfigs = map;

        if (map == null) {
            System.out.println("No additional configs found.");
            return;
        }

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

    @Override
    public int getReadingSize() { return readingSize; }

    @Override
    public FilteredProximityAgentSensor clone() {

        FilteredProximityAgentSensor cloned =
                new FilteredProximityAgentSensor(bearing, orientation, range, fieldOfView);

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
