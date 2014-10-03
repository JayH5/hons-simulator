package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.DetectedObject;
import za.redbridge.simulator.gp.types.GPFloat;
import za.redbridge.simulator.gp.types.GPFloatLiteral;

/**
 * Created by xenos on 9/10/14.
 */
public class TypeEQ extends Node {

    public TypeEQ(){
        this(null, null);
    }

    public TypeEQ(final Node c1, final Node c2){
        super(c1,c2);
    }
    @Override
    public String getIdentifier(){
        return "TYPEEQ";
    }

    public Boolean evaluate(){
        Object x = getChild(0).evaluate();
        Object y = getChild(1).evaluate();
        if(x.getClass() == DetectedObject.Type.class && y.getClass() == DetectedObject.Type.class) return x == y;
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if(inputTypes.length == 2 && inputTypes[0] == DetectedObject.Type.class && inputTypes[1] == DetectedObject.Type.class) return Boolean.class;
        else return null;
    }
}
