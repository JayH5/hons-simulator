package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.RelativeCoordinate;
import za.redbridge.simulator.gp.types.WheelDrive;

/**
 * Created by xenos on 9/10/14.
 */
public class CoordinateFromDistanceAndBearing extends Node {

    public CoordinateFromDistanceAndBearing(){
        this(null, null);
    }

    public CoordinateFromDistanceAndBearing(final Node c1, final Node c2){
        super(c1,c2);
    }
    @Override
    public String getIdentifier(){
        return "COORDINATEFROMDISTANCEANDBEARING";
    }

    public Object evaluate(){
        Object distance = getChild(0).evaluate();
        Object bearing = getChild(1).evaluate();
        if(TypeUtils.isNumericType(distance.getClass()) && bearing.getClass() == Bearing.class){
            return RelativeCoordinate.fromDistAndBearing(((Number)distance).floatValue(), (Bearing) bearing);
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 2 && TypeUtils.isNumericType(inputTypes[0]) && inputTypes[1] == Bearing.class) {
            return RelativeCoordinate.class;
        } else{
            return null;
        }
    }
}
