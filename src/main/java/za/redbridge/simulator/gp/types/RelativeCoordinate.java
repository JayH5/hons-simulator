package za.redbridge.simulator.gp.types;

import org.jbox2d.common.Vec2;

/**
 * Created by xenos on 9/11/14.
 */
public class RelativeCoordinate extends Vec2 {

    public RelativeCoordinate(float x, float y){
        super(x,y);
    }

    public static RelativeCoordinate fromDistAndBearing(float d, Bearing b){
        return new RelativeCoordinate((float) (d * Math.cos(b.getValue())), (float) (d * Math.sin(b.getValue())));
    }
}
