package za.redbridge.simulator.gp.types;

import java.text.DecimalFormat;

/**
 * Created by xenos on 9/11/14.
 */
public class ProximityReading {

    protected float value;

    public ProximityReading(float f){
        this.value = f;
    }

    public float getValue() {
        return value;
    }

    public String toString(){
        DecimalFormat df = new DecimalFormat("#.00");
        return "ProximityReading " + df.format(value);
    }
}
