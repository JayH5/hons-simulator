package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import za.redbridge.simulator.gp.types.*;

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

    public GPFloatVariable evaluate(){
        Object reading = getChild(0).evaluate();
        if(reading.getClass() == ProximityReading.class) {
            ProximityReading r = (ProximityReading)reading;
            return new GPFloatVariable(r.getDistance());
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && inputTypes[0] == ProximityReading.class) {
            return GPFloatVariable.class;
        } else{
            return null;
        }
    }
}
