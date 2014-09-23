package za.redbridge.simulator.gp.functions;

import org.epochx.epox.Node;
import za.redbridge.simulator.gp.types.Bearing;

import java.util.Map;

/**
 * Created by xenos on 9/10/14.
 */
public class LoadBearing extends Node {

    protected Map<Node,Map<String, Object>> state;
    protected final static String KEY = "BEARING";
    protected final static Bearing DEFAULT = new Bearing(0);
    protected final static Class KEY_TYPE = Bearing.class;

    public LoadBearing(Map<Node,Map<String, Object>> state){
        super();
        this.state = state;
    }
    @Override
    public String getIdentifier(){
        return "LOADBEARING";
    }

    public Bearing evaluate(){
        return (Bearing)state.get(this).getOrDefault(KEY,DEFAULT);
    }

    @Override
    public Class<?> getReturnType(final Class<?> ... inputTypes) {
        if (inputTypes.length == 0) {
            return Bearing.class;
        } else{
            return null;
        }
    }

    @Override
    public LoadBearing clone(){
        final LoadBearing clone = (LoadBearing) super.clone();
        clone.state = state;
        return clone;
    }

    @Override
    public LoadBearing newInstance() {
        return clone();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof LoadBearing)) {
            return false;
        }

        return ((LoadBearing) obj).state == state;
    }
}
