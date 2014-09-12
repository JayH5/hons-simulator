package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.RelativeCoordinate;

/**
 * Created by xenos on 9/10/14.
 */
public class ReadingToDistance extends Node {

    protected float range;

    public ReadingToDistance(float range){
        this(null, range);
    }

    public ReadingToDistance(final Node c1, float range){
        super(c1);
        this.range = range;
    }
    @Override
    public String getIdentifier(){
        return "READINGTODISTANCE";
    }

    public Object evaluate(){
        Object reading = getChild(0).evaluate();
        if(TypeUtils.isNumericType(reading.getClass())) {
            float c = ((Number)reading).floatValue();
            return (1.0f - c) * range;
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && TypeUtils.isNumericType(inputTypes[0].getClass())) {
            return Float.class;
        } else{
            return null;
        }
    }
}
