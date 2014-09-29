package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.ProximityReading;
import za.redbridge.simulator.gp.types.RelativeCoordinate;

/**
 * Created by xenos on 9/10/14.
 */
public class ReadingToDistance extends Node {

    protected float range;

    public ReadingToDistance(){
        this(null);
    }

    public ReadingToDistance(final Node c1){
        super(c1);
    }
    @Override
    public String getIdentifier(){
        return "READINGTODISTANCE";
    }

    public Float evaluate(){
        Object reading = getChild(0).evaluate();
        if(reading.getClass() == ProximityReading.class) {
            ProximityReading r = (ProximityReading)reading;
            return r.getDistance();
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && inputTypes[0] == ProximityReading.class) {
            return Float.class;
        } else{
            return null;
        }
    }
}
