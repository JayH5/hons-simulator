package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.WheelDrive;

/**
 * Created by xenos on 9/10/14.
 */
public class WheelDriveFromBearing extends Node {

    public WheelDriveFromBearing(){
        this(null);
    }

    public WheelDriveFromBearing(final Node c1){
        super(c1);
    }
    @Override
    public String getIdentifier(){
        return "WHEELDRIVEFROMBEARING";
    }

    public WheelDrive evaluate(){
        Object bearing = getChild(0).evaluate();
        if(bearing.getClass() == Bearing.class){
            return new WheelDrive((Bearing) bearing);
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && inputTypes[0] == Bearing.class) {
            return WheelDrive.class;
        } else{
            return null;
        }
    }
}
