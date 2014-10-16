package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.GPFloatVariable;
import za.redbridge.simulator.gp.types.ProximityReading;

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
        if(ProximityReading.class.isAssignableFrom(reading.getClass())) {
            ProximityReading r = (ProximityReading)reading;
            return new GPFloatVariable(r.getDistance());
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && ProximityReading.class.isAssignableFrom(inputTypes[0])) {
            return GPFloatVariable.class;
        } else{
            return null;
        }
    }
}
