package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.ProximityReading;
import za.redbridge.simulator.gp.types.RelativeCoordinate;

/**
 * Created by xenos on 9/10/14.
 */
public class ReadingToCoordinate extends Node {

    protected float range;

    public ReadingToCoordinate(){
        this(null);
    }

    public ReadingToCoordinate(final Node c1){
        super(c1);
    }
    @Override
    public String getIdentifier(){
        return "READINGTOCOORDINATE";
    }

    public RelativeCoordinate evaluate(){
        Object reading = getChild(0).evaluate();
        if(reading.getClass() == ProximityReading.class) {
            ProximityReading r = (ProximityReading)reading;
            return r.getCoordinate();
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && inputTypes[0] == ProximityReading.class) {
            return RelativeCoordinate.class;
        } else{
            return null;
        }
    }
}
