package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.GPFloatVariable;

/**
 * Created by xenos on 9/10/14.
 */
public class RandomGPFloat extends Node {

    public RandomGPFloat(){
        super();
    }

    @Override
    public String getIdentifier(){
        return "RANDOMFLOAT";
    }

    public GPFloatVariable evaluate(){
        return new GPFloatVariable((float)Math.random()*2 - 1);
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 0) {
            return GPFloatVariable.class;
        } else{
            return null;
        }
    }
}
