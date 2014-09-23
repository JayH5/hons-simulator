package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import org.jbox2d.common.Vec2;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.RelativeCoordinate;
import za.redbridge.simulator.phenotype.heuristics.Heuristic;

/**
 * Created by xenos on 9/10/14.
 */
public class BearingFromCoordinate extends Node {

    public BearingFromCoordinate(){
        this(null);
    }

    public BearingFromCoordinate(final Node c1){
        super(c1);
    }
    @Override
    public String getIdentifier(){
        return "BEARINGFROMCOORDINATE";
    }

    public Bearing evaluate(){
        Object coordinate = getChild(0).evaluate();
        if(coordinate.getClass() == RelativeCoordinate.class){
            RelativeCoordinate c = (RelativeCoordinate) coordinate;
            float b = (float) Math.atan2(c.x, c.y);
            return new Bearing(b);
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && inputTypes[0] == RelativeCoordinate.class) {
            return Bearing.class;
        } else{
            return null;
        }
    }
}
