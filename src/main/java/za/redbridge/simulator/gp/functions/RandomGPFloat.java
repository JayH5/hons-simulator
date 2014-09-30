package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.Bearing;
import za.redbridge.simulator.gp.types.GPFloat;

/**
 * Created by xenos on 9/10/14.
 */
public class RandomGPFloat extends Node {

    public RandomGPFloat(){
        super();
    }

    @Override
    public String getIdentifier(){
        return "RANDOMGPFLOAT";
    }

    public GPFloat evaluate(){
        return new GPFloat((float)Math.random()*2 - 1);
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 0) {
            return GPFloat.class;
        } else{
            return null;
        }
    }
}
