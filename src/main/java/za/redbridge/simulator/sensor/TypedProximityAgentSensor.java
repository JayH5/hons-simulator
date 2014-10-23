package za.redbridge.simulator.sensor;

import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by shsu on 2014/09/15.
 */
//Object types are analogous to colours
public class TypedProximityAgentSensor extends AgentSensor {

    protected static final int readingSize = 1;

    protected List<Class> senseClasses;

    protected Color paint;

    public TypedProximityAgentSensor(List<Class> classes) {
        super();
        senseClasses = classes;
    }

    public TypedProximityAgentSensor(List<Class> classes, float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
        this.senseClasses = classes;
    }

    public TypedProximityAgentSensor(List<Class> classes, float bearing) {
        super(bearing, 0.0f, 30.0f, 0.1f);
        this.senseClasses = classes;
    }

    //this constructor is meant for use with MorphologyConfig, which would then call readAdditionalConfigs with our detectables
    public TypedProximityAgentSensor(float bearing, float orientation, float range, float fieldOfView){
        this(null, bearing, orientation, range, fieldOfView);
    }

    @Override
    protected void provideObjectReading(List<SensedObject> objects, List<Double> output) {
        for(Class c : senseClasses){
            //find the closest object, then filter it by this class
            Optional<Double> reading = objects.stream().min((a,b) -> Float.compare(a.getDistance(), b.getDistance())).filter(o -> o.getClass() == c).map(o -> readingFromDistance(o.getDistance()));
            //if present, add reading. If closest object is not this type, add 0.0
            output.add(reading.orElse(0.0));
        }
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException {
        ArrayList<String> classStrings = (ArrayList<String>) map.get("detectables");
        if(classStrings == null){
            throw new RuntimeException("Could not find detectables key");
        }
        List<Class> detectables = new ArrayList<>();
        for(String classString : classStrings){
            try {
                detectables.add(Class.forName(classString));
            }catch(ClassNotFoundException e){
                throw new RuntimeException("Could not find class from config: " + classString);
            }
        }
        this.senseClasses = detectables;
    }

    protected double readingFromDistance(double distance){
        return 1 - Math.min(distance / range, 1.0);
    }

    public List<Class> getSenseClasses() {
        return senseClasses;
    }

    @Override
    protected int getFilterCategoryBits() {
        if (senseClasses.contains(TargetAreaObject.class)) {
            return FilterConstants.CategoryBits.TARGET_AREA_SENSOR;
        }

        return FilterConstants.CategoryBits.AGENT_SENSOR;
    }

    @Override
    protected int getFilterMaskBits() {
        return senseClasses.stream().map(c -> FilterConstants.CategoryBits.bitForClass(c)).reduce(0, (a,b) -> a | b);
    }


    @Override
    public int getReadingSize() { return readingSize; }

    @Override
    public TypedProximityAgentSensor clone() {

        TypedProximityAgentSensor cloned =
                new TypedProximityAgentSensor(senseClasses, bearing, orientation, range, fieldOfView);

        return cloned;
    }

    @Override
    public Map<String,Object> getAdditionalConfigs() { return additionalConfigs; }
}
