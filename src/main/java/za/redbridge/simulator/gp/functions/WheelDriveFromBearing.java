package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.WheelDrive;
import za.redbridge.simulator.phenotype.heuristics.Heuristic;

/**
 * Created by xenos on 9/10/14.
 */
public class WheelDriveFromBearing extends Node {

    public WheelDriveFromBearing(){
        this(null, null);
    }

    public WheelDriveFromBearing(final Node c1, final Node c2){
        super(c1,c2);
    }
    @Override
    public String getIdentifier(){
        return "WHEELDRIVEFROMBEARING";
    }

    public Object evaluate(){
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
