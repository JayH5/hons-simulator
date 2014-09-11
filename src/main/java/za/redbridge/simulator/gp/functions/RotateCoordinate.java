package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Vec2;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.RelativeCoordinate;

/**
 * Created by xenos on 9/10/14.
 */
public class RotateCoordinate extends Node {

    public RotateCoordinate(){
        this(null, null);
    }

    public RotateCoordinate(final Node c1, final Node c2){
        super(c1,c2);
    }
    @Override
    public String getIdentifier(){
        return "ROTATECOORDINATE";
    }

    public Object evaluate(){
        Object coordinate = getChild(0).evaluate();
        Object bearing = getChild(1).evaluate();
        if(coordinate.getClass() == RelativeCoordinate.class && bearing.getClass() == Bearing.class){
            Bearing b = (Bearing) bearing;
            RelativeCoordinate c = (RelativeCoordinate) coordinate;
            Rot rotation = new Rot(((Bearing) bearing).getValue());
            Vec2 result = new Vec2();
            Rot.mulToOut(rotation, new Vec2(c.x, c.y), result);
            return new RelativeCoordinate(result.x, result.y);
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 2 && inputTypes[0] == RelativeCoordinate.class && inputTypes[1] == Bearing.class) {
            return RelativeCoordinate.class;
        } else{
            return null;
        }
    }
}
