package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.GPFloatLiteral;
import za.redbridge.simulator.gp.types.GPFloat;

/**
 * Created by xenos on 9/10/14.
 */
public class GT extends Node {

    public GT(){
        this(null, null);
    }

    public GT(final Node c1, final Node c2){
        super(c1,c2);
    }
    @Override
    public String getIdentifier(){
        return "GT";
    }

    public Boolean evaluate(){
        Object x = getChild(0).evaluate();
        Object y = getChild(1).evaluate();
        if(isLegit(x.getClass(), y.getClass())) return ((GPFloat) x).getValue() > ((GPFloat) y).getValue();
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if(inputTypes.length == 2 && isLegit(inputTypes[0], inputTypes[1])) return Boolean.class;
        else return null;
    }

    protected boolean isLegit(Class a, Class b){
        return GPFloat.isNumeric(a) && GPFloat.isNumeric(b) && !(a == GPFloatLiteral.class && b == GPFloatLiteral.class);
    }
}
