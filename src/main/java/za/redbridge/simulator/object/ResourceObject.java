package za.redbridge.simulator.object;

import java.awt.geom.Rectangle2D;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import za.redbridge.simulator.portrayal.RectanglePortrayal2D;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject implements Steppable {

    private final RectanglePortrayal2D portrayal;

    private final double value;
    private final double weight;

    private final Rectangle2D.Double collisionRectangle = new Rectangle2D.Double();

    private Double2D position;

    public ResourceObject(Double2D initialPosition, double width, double height, double value,
            double weight) {
        position = initialPosition;
        portrayal = new RectanglePortrayal2D(width, height);
        this.value = value;
        this.weight = weight;
    }

    public double getValue() {
        return value;
    }

    public double getWeight() {
        return weight;
    }

    public RectanglePortrayal2D getPortrayal() {
        return portrayal;
    }

    @Override
    public void step(SimState state) {
        // TODO: Do something?
    }

    public Rectangle2D.Double getBoundingRectangle() {
        double x = position.x - portrayal.getWidth() / 2;
        double y = position.y - portrayal.getHeight() / 2;

        collisionRectangle.setFrame(x, y, portrayal.getWidth(), portrayal.getHeight());

        return collisionRectangle;
    }
}
