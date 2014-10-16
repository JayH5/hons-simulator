package za.redbridge.simulator.gp;

import org.epochx.epox.Variable;
import org.epochx.tools.util.TypeUtils;

/**
 * Created by xenos on 9/29/14.
 * A class to tie the sensor parameters to the input variable; it will then produce ProximityReading objects with the same parameters set.
 * This allows us to convert ProximityReading objects to their RelativeCoordinate, Bearing and FloatVariable (distance) counterparts.
 */
public class ThresholdedSensorVariable extends Variable {
    private float threshold;

    public ThresholdedSensorVariable(String identifier, float threshold) {
        super(identifier, Boolean.class);
        this.threshold = threshold;
    }

    public void setValue(Object value){
        if (!TypeUtils.isNumericType(value.getClass())) {
            throw new IllegalArgumentException("Variable is not numeric");
        }
        float floatValue = ((Number) value).floatValue();
        super.setValue(new Boolean(floatValue > threshold));
    }
}
