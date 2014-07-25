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
public class ResourceObject extends PhysicalObject<RectanglePortrayal2D> {

    private final double value;

    private final Rectangle2D.Double collisionRectangle = new Rectangle2D.Double();

    public ResourceObject(double mass, double width, double height, Double2D position,
            double value) {
        super(mass, new RectanglePortrayal2D(width, height), position);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public Rectangle2D.Double getBoundingRectangle() {
        double x = position.x - portrayal.getWidth() / 2;
        double y = position.y - portrayal.getHeight() / 2;

        collisionRectangle.setFrame(x, y, portrayal.getWidth(), portrayal.getHeight());

        return collisionRectangle;
    }

    @Override
    protected RectangularShape createCollisionShape() {
        return new Rectangle2D.Double(position.x, position.y, portrayal.getWidth(),
                portrayal.getHeight());
    }
}
