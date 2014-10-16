package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.Bearing;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xenos on 9/10/14.
 */
public class SaveBearing extends Node {

    protected Map<Node,Map<String, Object>> state;
    protected final static String KEY = "BEARING";

    public SaveBearing(Map<Node,Map<String,Object>> state){
        this(state, null);
    }

    public SaveBearing(Map<Node,Map<String,Object>> state, final Node c1){
        super(c1);
        this.state = state;
    }
    @Override
    public String getIdentifier(){
        return "SAVEBEARING";
    }

    public Bearing evaluate(){
        Object bearing = getChild(0).evaluate();
        if(bearing.getClass() == Bearing.class){
            Bearing b = (Bearing) bearing;
            if(!state.containsKey(this)) state.put(this,new HashMap<String,Object>());
            state.get(this).put(KEY, b);
            return b;
        }
        else return null;
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 1 && inputTypes[0] == Bearing.class) {
            return Bearing.class;
        } else{
            return null;
        }
    }

    @Override
    public SaveBearing clone(){
        final SaveBearing clone = (SaveBearing) super.clone();
        clone.state = state;
        return clone;
    }

    @Override
    public SaveBearing newInstance() {
        return clone();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SaveBearing)) {
            return false;
        }

        return ((SaveBearing) obj).state == state;
    }
}
