package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.DetectedObject;
import za.redbridge.simulator.gp.types.ProximityReading;
import za.redbridge.simulator.gp.types.TypedProximityReading;

/**
 * Created by xenos on 9/10/14.
 */
public class ReadingIsOfType extends Node {

    protected float range;

    public ReadingIsOfType(){
        this(null, null);
    }

    public ReadingIsOfType(final Node c1, final Node c2){
        super(c1, c2);
    }

    @Override
    public String getIdentifier(){
        return "READINGISOFTYPE";
    }

    public Boolean evaluate(){
        Object reading = getChild(0).evaluate();
        Object type = getChild(1).evaluate();
        if(reading.getClass() == TypedProximityReading.class && type.getClass() == DetectedObject.Type.class) {
            TypedProximityReading r = (TypedProximityReading)reading;
            return r.getReadingType() == (DetectedObject.Type)type;
        }
        else {
            return null;
        }
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 2 && inputTypes[0] == TypedProximityReading.class && inputTypes[1] == DetectedObject.Type.class) {
            return Boolean.class;
        } else{
            return null;
        }
    }
}
