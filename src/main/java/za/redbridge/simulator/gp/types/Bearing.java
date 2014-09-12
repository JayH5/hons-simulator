package za.redbridge.simulator.gp.types;

import java.text.DecimalFormat;

/**
 * Created by xenos on 9/11/14.
 */
public class Bearing{

    protected float value;

    public Bearing(float f){
        this.value = normalizeBearing(f);
    }

    public float getValue() {
        return value;
    }

    public Bearing add(Bearing b){
        value = normalizePositiveBearing(value + b.getValue());
        return this;
    }

    private static float normalizeBearing(float bearing){
        return (float)(( bearing % (2 * Math.PI) + 4 * Math.PI) % (2 * Math.PI));
    }

    private static float normalizePositiveBearing(float bearing) {
        return (float) (bearing % (2 * Math.PI));
    }

    public String toString(){
        DecimalFormat df = new DecimalFormat("#.00");
        return "Bearing " + df.format(value);
    }
}
