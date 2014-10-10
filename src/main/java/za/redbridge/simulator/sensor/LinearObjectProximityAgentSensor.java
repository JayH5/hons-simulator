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
    private double detectivity;

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

        for(SensedObject o : objects){

            if (o.getObject() instanceof RobotObject) {

                //width or height
                double signal = ((Arc2D) o.getShape()).getHeight();

                robotReading = 1 - Math.min(o.getDistance() / range, 1.0);
            }
            else if (o.getObject() instanceof ResourceObject) {

                double signal = ((Rectangle2D) o.getShape()).getHeight();

                resourceReading = 1 - Math.min(o.getDistance() / range, 1.0);
            }
            else if (o.getObject() instanceof TargetAreaObject) {

                double signal = ((Rectangle2D) o.getShape()).getHeight();

                targetAreaReading = 1 - Math.min(o.getDistance() / range, 1.0);
            }
            else if (o.getObject() instanceof WallObject) {

                double signal = o.getDistance();

                wallReading = 1 - Math.min(o.getDistance() / range, 1.0);
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

        Number sens = (Number) map.get("detectivity");
        if (checkFieldPresent(sens, "detectivity")) {
            double sensValue = sens.doubleValue();

            if (sensValue > 1 || sensValue < 0) {
                throw new ParseException("Detectivity value for ObjectProximitySensor must be between 0 and 1", 0);
            }

            this.detectivity = sensValue;
        }
        else {
            throw new ParseException("No detectivity value found for ObjectProximitySensor.", 0);
        }
    }

    public void setDetectivity(double detectivity) { this.detectivity = detectivity; }

    public double getDetectivity() { return detectivity; }

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
