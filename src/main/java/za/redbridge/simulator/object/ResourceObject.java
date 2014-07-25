package za.redbridge.simulator.object;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import sim.util.Double2D;
import za.redbridge.simulator.portrayal.RectanglePortrayal2D;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject extends PhysicalObject {

    private final double value;

    public ResourceObject(double mass, double width, double height, Double2D position,
            double value) {
        super(mass, new RectanglePortrayal2D(width, height), position);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    protected RectangularShape createCollisionShape() {
        RectanglePortrayal2D portrayal = (RectanglePortrayal2D) getPortrayal();
        return new Rectangle2D.Double(0.0, 0.0, portrayal.getWidth(),
                portrayal.getHeight());
    }
}
