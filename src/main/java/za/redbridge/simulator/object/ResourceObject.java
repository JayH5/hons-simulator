package za.redbridge.simulator.object;

import java.awt.Paint;

import sim.physics2D.shape.Rectangle;
import sim.physics2D.util.Angle;
import sim.util.Double2D;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject extends PhysicalObject {

    private final double value;

    public ResourceObject(Double2D position, double mass, double width, double height, Paint paint,
            double value) {
        setPose(position, new Angle(0));
        setShape(new Rectangle(width, height, paint), mass);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

}
