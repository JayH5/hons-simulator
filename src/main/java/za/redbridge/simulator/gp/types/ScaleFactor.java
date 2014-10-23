package za.redbridge.simulator.gp.types;

import java.text.DecimalFormat;

public class ScaleFactor {
    protected float value;
    public ScaleFactor(float value){
        this.value = value;
    }
    public float getValue(){
        return value;
    }

    public String toString(){
        DecimalFormat df = new DecimalFormat("#.00");
        return "SF" + df.format(value);
    }
}
