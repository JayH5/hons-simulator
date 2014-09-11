package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import org.jbox2d.common.Vec2;
import za.redbridge.simulator.gp.types.RelativeCoordinate;
import za.redbridge.simulator.gp.types.WheelDrive;
import za.redbridge.simulator.phenotype.heuristics.Heuristic;

/**
 * Created by xenos on 9/10/14.
 */
public class WheelDriveFromCoordinate extends Node {

    public WheelDriveFromCoordinate(){
        this(null);
    }

    public WheelDriveFromCoordinate(final Node c1){
        super(c1);
    }
    @Override
    public String getIdentifier(){
        return "WHEELDRIVEFROMCOORD";
    }

    public Object evaluate(){
        Object coord = getChild(0).evaluate();
        if(coord.getClass() == RelativeCoordinate.class){
            return new WheelDrive((RelativeCoordinate) coord);
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && inputTypes[0] == RelativeCoordinate.class) {
            return WheelDrive.class;
        } else{
            return null;
        }
    }
}
