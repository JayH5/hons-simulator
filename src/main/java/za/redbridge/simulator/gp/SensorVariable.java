package za.redbridge.simulator.gp;

import org.epochx.epox.Variable;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.ProximityReading;
import za.redbridge.simulator.gp.types.RelativeCoordinate;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.Sensor;

/**
 * Created by xenos on 9/29/14.
 */
public class SensorVariable extends Variable {
    private final float bearing;
    private final float range;
    private final float fov;
    private Class innerDatatype;

    public SensorVariable(String identifier, Class<?> datatype, AgentSensor sensor) {
        super(identifier, ProximityReading.class);
        this.bearing = sensor.getBearing();
        this.range = sensor.getRange();
        this.fov = sensor.getFieldOfView();
        this.innerDatatype = datatype;
    }

    public SensorVariable(String identifier, Class<?> datatype, float range, float bearing, float fov) {
        super(identifier, datatype);
        this.range = range;
        this.bearing = bearing;
        this.fov = fov;
    }

    public void setValue(Object value){
        if (value != null && !innerDatatype.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Variables may not change data-type");
        }
        super.setValue(new ProximityReading(((Number)value).floatValue(), bearing, range, fov));
    }
}
