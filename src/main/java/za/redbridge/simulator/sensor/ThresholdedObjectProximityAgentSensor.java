package za.redbridge.simulator.sensor;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.portrayal.ConePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/09/15.
 */
//Object types are analogous to colours
public class ThresholdedObjectProximityAgentSensor extends AdjustableSensitivityAgentSensor {

    protected static final int readingSize = 1;
    protected double sensitivity;

    protected String sensitiveClass;

    protected Class senseClass;

    protected Color paint;

    public ThresholdedObjectProximityAgentSensor() {
        super();

        sensitivity = 0;
        try {
            senseClass = Class.forName("za.redbridge.simulator.object.ResourceObject");
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
            this.senseClass = Class.forName(sensitiveClassName);
        }catch(ClassNotFoundException e){}

    }

    protected void setPaint () {

        int red = 0;
        int blue = 0;
        int green = 0;

        float value = 1f-((float) sensitivity);
        int alpha = (int) (value*255f);

        String className = senseClass.getName();

        if (className.equals("za.redbridge.simulator.object.RobotObject")) {
            blue = 34;
            green = 255;
        }
        else if (className.equals("za.redbridge.simulator.object.ResourceObject")) {

            red = 255;
            blue = 208;
        }
        else if (className.equals("za.redbridge.simulator.object.TargetAreaObject")) {

            red = 69;
            blue = 138;
        }
        else if (className.equals("za.redbridge.simulator.object.WallObject")) {

            red = 69;
            blue = 0;
            green = 138;
        }
        paint = new Color(red, blue, green, alpha);
    }

    @Override
    protected Paint getPaint() {
        if (paint == null)
            setPaint();

        return paint;
    }

    @Override
    protected void provideObjectReading(List<SensedObject> objects, List<Double> output) {

        double reading = 0.0;

        for(SensedObject o : objects){
            if(o.getObject().getClass().equals(senseClass)){
                reading = 1 - Math.min(o.getDistance() / range, 1.0);
                break;
            }
        }

        //threshold
        if (reading >= (1-sensitivity)) {
            output.add(reading);
        }
        else {
            output.add(0.0);
        }
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
                senseClass = Class.forName(sensitive);
                sensitiveClass = sensitive;
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

        this.setPaint();
    }

    @Override
    protected int getFilterCategoryBits() {
        if (senseClass.getSimpleName().contains("TargetAreaObject")) {
            return FilterConstants.CategoryBits.TARGET_AREA_SENSOR;
        }

        return FilterConstants.CategoryBits.AGENT_SENSOR;
    }

    @Override
    protected int getFilterMaskBits() {

        String className = senseClass.getSimpleName();

        if (className.equals("RobotObject")) {
            return FilterConstants.CategoryBits.ROBOT;
        }
        else if (className.equals("ResourceObject")) {
            return FilterConstants.CategoryBits.RESOURCE;
        }
        else if (className.equals("TargetAreaObject")) {
            return FilterConstants.CategoryBits.TARGET_AREA;
        }
        else if (className.equals("WallObject")) {
            return FilterConstants.CategoryBits.WALL;
        }
        else {
            return FilterConstants.CategoryBits.DEFAULT;
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

    @Override
    public void setSensitivity(double sensitivity) { this.sensitivity = sensitivity; }

    @Override
    public double getSensitivity() { return sensitivity; }

    public String getSensitiveClass() { return senseClass.getSimpleName(); }

    @Override
    public Map<String,Object> getAdditionalConfigs() { return additionalConfigs; }

}
