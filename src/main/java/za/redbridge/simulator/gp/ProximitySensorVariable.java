package za.redbridge.simulator.gp;

import org.epochx.epox.Variable;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.ProximityReading;
import za.redbridge.simulator.gp.types.RelativeCoordinate;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.Sensor;

/**
 * Created by xenos on 9/29/14.
 * A class to tie the sensor parameters to the input variable; it will then produce ProximityReading objects with the same parameters set.
 * This allows us to convert ProximityReading objects to their RelativeCoordinate, Bearing and FloatVariable (distance) counterparts.
 */
public class ProximitySensorVariable extends Variable {
    private Class innerDatatype;
    private AgentSensor sensor;

    public ProximitySensorVariable(String identifier, Class<?> datatype, AgentSensor sensor) {
        super(identifier, ProximityReading.class);
        this.sensor = sensor;
        this.innerDatatype = datatype;
    }

    public void setValue(Object value){
        if (value != null && !innerDatatype.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Variables may not change data-type");
        }
        super.setValue(new ProximityReading(((Number) value).floatValue(), sensor));
    }
}
