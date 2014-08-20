package za.redbridge.simulator.sensor;

import java.util.*;

public class FilteredProximitySensor extends Sensor {

    private final List<Double> readings = new ArrayList<>(1);
    private Set<Class> whitelist = null;

    public FilteredProximitySensor(float bearing, Collection<String> whitelist) {
        super(bearing, 0.0f, 30.0f, 0.1f);
        for(String cs : whitelist){
            try {
                this.whitelist.add(Class.forName(cs));
            }catch(ClassNotFoundException e){}
        }
    }

    public FilteredProximitySensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected SensorReading provideReading(List<SensedObject> objects) {
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
}