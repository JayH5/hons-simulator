package za.redbridge.simulator.object;

import java.awt.Color;

import sim.physics2D.physicalObject.StationaryObject2D;
import sim.physics2D.shape.Rectangle;
import sim.physics2D.util.Angle;
import sim.util.Double2D;

/**
 * Created by jamie on 2014/08/01.
 */
public class WallObject extends StationaryObject2D {

    public WallObject(Double2D pos, int width, int height) {
        setCoefficientOfRestitution(1);
        setPose(pos, new Angle(0));
        setShape(new Rectangle(width, height, Color.BLACK, true));
    }
}
