package za.redbridge.simulator.gp;

import org.epochx.epox.Variable;
import za.redbridge.simulator.gp.types.DetectedObject;
import za.redbridge.simulator.gp.types.TypedProximityReading;
import za.redbridge.simulator.sensor.AgentSensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xenos on 9/29/14.
 * A class to tie the sensor parameters to the input variable; it will then produce ProximityReading objects with the same parameters set.
 * This allows us to convert ProximityReading objects to their RelativeCoordinate, Bearing and FloatVariable (distance) counterparts.
 */
public class TypedProximitySensorVariable extends Variable {
    private final List<DetectedObject.Type> sensorOrder;
    private AgentSensor sensor;

    public TypedProximitySensorVariable(String identifier, List<DetectedObject.Type> sensorOrder, AgentSensor sensor) {
        super(identifier, TypedProximityReading.class);
        this.sensor = sensor;
        this.sensorOrder = sensorOrder;
    }

    public void setValue(Object value){
        if (value == null || !List.class.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Value needs to be of type List<Float>, got: " + value.getClass());
        }
        List<Float> values = (List<Float>)value;
        Map valueMap = new HashMap();
        for(int i = 0; i < Math.max(sensorOrder.size(), values.size()); i++){
            valueMap.put(sensorOrder.get(i), values.get(i));
        }
        super.setValue(new TypedProximityReading(valueMap, sensor));
    }
}
