package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import za.redbridge.simulator.gp.types.WheelDrive;

/**
 * Created by xenos on 9/10/14.
 */
public class WheelDriveFromFloats extends Node {

    public WheelDriveFromFloats(){
        this(null, null);
    }

    public WheelDriveFromFloats(final Node c1, final Node c2){
        super(c1,c2);
    }
    @Override
    public String getIdentifier(){
        return "DOUBLE2D";
    }

    public Object evaluate(){
        Object x = getChild(0).evaluate();
        Object y = getChild(1).evaluate();
        if(TypeUtils.isNumericType(x.getClass()) && TypeUtils.isNumericType(y.getClass())){
            return new WheelDrive(((Number) x).floatValue(), ((Number) y).floatValue());
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 2 && TypeUtils.isNumericType(inputTypes[0]) && TypeUtils.isNumericType(inputTypes[1])) {
            return WheelDrive.class;
        } else{
            return null;
        }
    }
}
