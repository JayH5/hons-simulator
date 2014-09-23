package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.WheelDrive;

/**
 * Created by xenos on 9/10/14.
 */
public class RandomBearing extends Node {

    public RandomBearing(){
        super();
    }

    @Override
    public String getIdentifier(){
        return "RANDOMBEARING";
    }

    public Bearing evaluate(){
        return new Bearing((float)(Math.random()*2*Math.PI));
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 0) {
            return Bearing.class;
        } else{
            return null;
        }
    }
}
