package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.GPFloat;
import za.redbridge.simulator.gp.types.WheelDrive;

import java.util.Optional;

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
        return "WHEELDRIVEFROMFLOATS";
    }

    public WheelDrive evaluate(){
        Optional<Float> x = GPFloat.maybeFloat(getChild(0).evaluate());
        Optional<Float> y = GPFloat.maybeFloat(getChild(1).evaluate());
        return x.flatMap(ox -> y.map(oy -> new WheelDrive(ox,oy))).orElse(null);
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 2 && GPFloat.isNumeric(inputTypes[0]) && GPFloat.isNumeric(inputTypes[1])){
            return WheelDrive.class;
        } else{
            return null;
        }
    }
}
