package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.ScaleFactor;
import za.redbridge.simulator.gp.types.WheelDrive;

/**
 * Created by xenos on 9/10/14.
 */
public class ScaleWheelDrive extends Node {

    protected float range;

    public ScaleWheelDrive(){
        this(null,null);
    }

    public ScaleWheelDrive(final Node c1, final Node c2){
        super(c1,c2);
    }
    @Override
    public String getIdentifier(){
        return "SCALEWHEELDRIVE";
    }

    public WheelDrive evaluate(){
        Object w = getChild(0).evaluate();
        Object v = getChild(1).evaluate();
        if(WheelDrive.class.isAssignableFrom(w.getClass()) && ScaleFactor.class.isAssignableFrom(v.getClass())) {
            WheelDrive wheelDrive = (WheelDrive) w;
            ScaleFactor scaleFactor = (ScaleFactor) v;
            return new WheelDrive(wheelDrive.x * scaleFactor.getValue(), wheelDrive.y * scaleFactor.getValue());
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 2 && WheelDrive.class.isAssignableFrom(inputTypes[0]) && ScaleFactor.class.isAssignableFrom(inputTypes[1])) {
            return WheelDrive.class;
        } else{
            return null;
        }
    }
}
