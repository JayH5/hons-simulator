package za.redbridge.simulator.sensor;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/10/13.
 */
public class ThresholdedObjectProximityAgentSensor extends AdjustableSensitivityAgentSensor {

    private static final int readingSize = 4;
    private double[] detectivity;

    public ThresholdedObjectProximityAgentSensor() {

    }

    public ThresholdedObjectProximityAgentSensor(float bearing, float orientation, float range, float fieldOfView) {
        super(bearing, orientation, range, fieldOfView);
    }

    @Override
    protected void provideObjectReading(List<SensedObject> objects, List<Double> output) {

        double robotReading = 0.0;
        double resourceReading = 0.0;
        double targetAreaReading = 0.0;
        double wallReading = 0.0;

        for(SensedObject o : objects) {

            if (o.getObject() instanceof RobotObject) {

                robotReading = (1 - Math.min(o.getDistance() / range, 1.0));
                robotReading = robotReading > 1 - detectivity[0] ? robotReading : 0;
            } else if (o.getObject() instanceof ResourceObject) {

                resourceReading = (1 - Math.min(o.getDistance() / range, 1.0));
                resourceReading = resourceReading > 1 - detectivity[1] ? resourceReading : 0;
            } else if (o.getObject() instanceof TargetAreaObject) {

                targetAreaReading = (1 - Math.min(o.getDistance() / range, 1.0));
                targetAreaReading = targetAreaReading > 1 - detectivity[2] ? resourceReading : 0;
            } else if (o.getObject() instanceof WallObject) {

                wallReading = (1 - Math.min(o.getDistance() / range, 1.0));
                wallReading = wallReading > 1 - detectivity[3] ? wallReading : 0;
            }
        }

        output.add(robotReading);
        output.add(resourceReading);
        output.add(targetAreaReading);
        output.add(wallReading);
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException {

        additionalConfigs = map;
        detectivity = new double[readingSize];

        Number detectivity = (Number) map.get("robotSensorDetectivity");
        if (checkFieldPresent(detectivity, "robotSensorDetectivity")) {
            double detValue = detectivity.doubleValue();

            if (detValue < 0 || detValue > 1) {
                throw new ParseException("Detectivity value for Robot Sensor must be between 0 and 1", 0);
            }

            this.detectivity[0] = detValue;
        }
        else {
            throw new ParseException("No detectivity value found for Robot Sensor.", 0);
        }

        detectivity = (Number) map.get("resourceSensorDetectivity");
        if (checkFieldPresent(detectivity, "resourceSensorDetectivity")) {
            double detValue = detectivity.doubleValue();

            if (detValue < 0 || detValue > 1) {
                throw new ParseException("Detectivity value for Resource Sensor must be between 0 and 1", 0);
            }

            this.detectivity[1] = detValue;
        }
        else {
            throw new ParseException("No detectivity value found for Resource Sensor.", 0);
        }

        detectivity = (Number) map.get("targetAreaSensorDetectivity");
        if (checkFieldPresent(detectivity, "targetAreaSensorDetectivity")) {
            double detValue = detectivity.doubleValue();

            if (detValue < 0 || detValue > 1) {
                throw new ParseException("Detectivity value for Target Area Sensor must be between 0 and 1", 0);
            }

            this.detectivity[2] = detValue;
        }
        else {
            throw new ParseException("No detectivity value found for Target Area Sensor.", 0);
        }

        detectivity = (Number) map.get("wallSensorDetectivity");
        if (checkFieldPresent(detectivity, "wallSensorDetectivity")) {
            double detValue = detectivity.doubleValue();

            if (detValue < 0 || detValue > 1) {
                throw new ParseException("Detectivity value for Wall Sensor must be between 0 and 1", 0);
            }

            this.detectivity[3] = detValue;
        }
        else {
            throw new ParseException("No detectivity value found for Wall Sensor.", 0);
        }

    }


    public void setDetectivity(double[] detectivity) { this.detectivity = detectivity; }

    public double[] getDetectivity() { return detectivity; }

    @Override
    public Map<String,Object> getAdditionalConfigs() { return additionalConfigs; }

    @Override
    public int getReadingSize() { return readingSize; }

    @Override
    public ThresholdedObjectProximityAgentSensor clone() {

        ThresholdedObjectProximityAgentSensor cloned = new ThresholdedObjectProximityAgentSensor(bearing, orientation, range, fieldOfView);

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

    public String parametersToString() {

        String output = "";

        for (double value: detectivity) {
            output += value + " ";
        }

        return output;
    }
}
