package za.redbridge.simulator.sensor;

import com.sun.deploy.model.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.sensor.sensedobjects.CircleSensedObject;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/10/08.
 */
public class LinearObjectProximityAgentSensor extends AgentSensor {

    private static final int readingSize = 4;
    private double[] gain;
    private double[] detectivity;

    public LinearObjectProximityAgentSensor() {

    }

    public LinearObjectProximityAgentSensor(float bearing, float orientation, float range, float fieldOfView) {
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

                robotReading = (1 - Math.min(o.getDistance() / range, 1.0))*gain[0];
            } else if (o.getObject() instanceof ResourceObject) {

                resourceReading = (1 - Math.min(o.getDistance() / range, 1.0))*gain[1];
            } else if (o.getObject() instanceof TargetAreaObject) {

                targetAreaReading = (1 - Math.min(o.getDistance() / range, 1.0))*gain[2];
            } else if (o.getObject() instanceof WallObject) {

                wallReading = (1 - Math.min(o.getDistance() / range, 1.0))*gain[3];
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

        gain = new double[readingSize];
        detectivity = new double[readingSize];

        Number gain = (Number) map.get("robotSensorGain");
        if (checkFieldPresent(gain, "robotSensorGain")) {
            double gain0Value = gain.doubleValue();

            if (gain0Value < 0 || gain0Value > 2) {
                throw new ParseException("Gain value for Robot Sensor must be between 0 and 2", 0);
            }

            this.gain[0] = gain0Value;
        }
        else {
            throw new ParseException("No gain value found for Robot Sensor.", 0);
        }

        gain = (Number) map.get("resourceSensorGain");
        if (checkFieldPresent(gain, "resourceSensorGain")) {
            double gain0Value = gain.doubleValue();

            if (gain0Value < 0 || gain0Value > 2) {
                throw new ParseException("Gain value for Resource Sensor must be between 0 and 2", 0);
            }

            this.gain[1] = gain0Value;
        }
        else {
            throw new ParseException("No gain value found for Resource Sensor.", 0);
        }

        gain = (Number) map.get("targetAreaSensorGain");
        if (checkFieldPresent(gain, "targetAreaSensorGain")) {
            double gain0Value = gain.doubleValue();

            if (gain0Value < 0 || gain0Value > 2) {
                throw new ParseException("Gain value for Target Area Sensor must be between 0 and 2", 0);
            }

            this.gain[2] = gain0Value;
        }
        else {
            throw new ParseException("No gain value found for Target Area Sensor.", 0);
        }

        gain = (Number) map.get("wallSensorGain");
        if (checkFieldPresent(gain, "wallSensorGain")) {
            double gain0Value = gain.doubleValue();

            if (gain0Value < 0 || gain0Value > 2) {
                throw new ParseException("Gain value for Wall Sensor must be between 0 and 2", 0);
            }

            this.gain[3] = gain0Value;
        }
        else {
            throw new ParseException("No gain value found for Wall Sensor.", 0);
        }

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

    public void setGain(double[] gain) { this.gain = gain; }

    public double[] getGain() { return gain; }

    public void setDetectivity(double[] detectivity) { this.detectivity = detectivity; }

    public double[] getDetectivity() { return detectivity; }

    @Override
    public Map<String,Object> getAdditionalConfigs() { return additionalConfigs; }

    @Override
    public int getReadingSize() { return readingSize; }

    @Override
    public LinearObjectProximityAgentSensor clone() {

        LinearObjectProximityAgentSensor cloned = new LinearObjectProximityAgentSensor(bearing, orientation, range, fieldOfView);

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
