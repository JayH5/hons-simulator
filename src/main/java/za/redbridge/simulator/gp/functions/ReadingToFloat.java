package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.ProximityReading;

/**
 * Created by xenos on 9/10/14.
 */
public class ReadingToFloat extends Node {

    protected float range;

    public ReadingToFloat(){
        this(null);
    }

    public ReadingToFloat(final Node c1){
        super(c1);
    }
    @Override
    public String getIdentifier(){
        return "READINGTOFLOAT";
    }

    public Float evaluate(){
        Object reading = getChild(0).evaluate();
        if(reading.getClass() == ProximityReading.class) {
            ProximityReading r = (ProximityReading)reading;
            return r.getValue();
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
