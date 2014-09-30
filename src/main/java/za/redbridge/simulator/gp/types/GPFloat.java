package za.redbridge.simulator.gp.types;

import java.util.Optional;

/**
 * Created by xenos on 9/30/14.
 * An abstraction of a float; done for the ability to differentiate float literals (which are disallowed in both branches of a GT) and variable-derived floats.
 * The actual Float class is banned from our GP.
 */
public abstract class GPFloat {
    protected float value;
    public GPFloat(float value){
        this.value = value;
    }
    public float getValue(){
        return value;
    }

    public static Optional<Float> maybeFloat(Object x){
        if(isNumeric(x.getClass())) return Optional.of(((GPFloat) x).getValue());
        else return Optional.empty();
    }

    public static boolean isNumeric(Class x){
        return GPFloat.class.isAssignableFrom(x) && x != GPFloat.class;
    }
}
