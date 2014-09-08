package za.redbridge.simulator.portrayal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Provides a simple portrayal of a rectangular object.
 *
 * Created by jamie on 2014/07/23.
 */
public class RectanglePortrayal extends PolygonPortrayal {

    private transient Rectangle2D preciseRect;

    private final double width;
    private final double height;

    public RectanglePortrayal(double width, double height) {
        this(width, height, Color.BLACK, true);
    }

    public RectanglePortrayal(double width, double height, Paint paint, boolean filled) {
        super(4, paint, filled);
        this.width = width;
        this.height = height;

        float halfWidth = (float) (width / 2);
        float halfHeight = (float) (height / 2);
        vertices[0].set(-halfWidth, -halfHeight);
        vertices[1].set(halfWidth, -halfHeight);
        vertices[2].set(halfWidth, halfHeight);
        vertices[3].set(-halfWidth, halfHeight);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    protected void drawPrecise(Graphics2D graphics, STRTransform transform,
            boolean transformUpdated) {
        if (preciseRect == null) {
            preciseRect = new Rectangle2D.Double(-width / 2.0, -height / 2.0, width, height);
        }

        Shape transformedShape = transform.getAffineTransform().createTransformedShape(preciseRect);

        if (filled) {
            graphics.fill(transformedShape);
        } else {
            graphics.draw(transformedShape);
        }
    }

}