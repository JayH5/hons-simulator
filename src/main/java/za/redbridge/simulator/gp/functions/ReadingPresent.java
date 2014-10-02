package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.ProximityReading;

/**
 * Created by xenos on 9/10/14.
 */
public class ReadingPresent extends Node {

    protected float range;

    public ReadingPresent(){
        this(null);
    }

    public ReadingPresent(final Node c1){
        super(c1);
    }
    @Override
    public String getIdentifier(){
        return "READINGTOBOOLEAN";
    }

    public Boolean evaluate(){
        Object reading = getChild(0).evaluate();
        if(reading.getClass() == ProximityReading.class) {
            ProximityReading r = (ProximityReading)reading;
            return r.getValue() > 0.0;
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && inputTypes[0] == ProximityReading.class) {
            return Boolean.class;
        } else{
            return null;
        }
    }
}
