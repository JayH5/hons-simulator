package za.redbridge.simulator.portrayal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Provides a simple portrayal of a rectangular object.
 *
 * Created by jamie on 2014/07/23.
 */
public class RectanglePortrayal extends PolygonPortrayal {

    private transient Rectangle2D.Double preciseRect;

    private final double width;
    private final double height;

    public RectanglePortrayal(double width, double height) {
        this(width, height, Color.BLACK, true);
    }

    public RectanglePortrayal(double width, double height, Paint paint, boolean filled) {
        super(4, paint, filled);
        this.width = width;
        this.height = height;

        double halfWidth = width / 2;
        double halfHeight = height / 2;
        vertices[0].setLocation(-halfWidth, -halfHeight);
        vertices[1].setLocation(halfWidth, -halfHeight);
        vertices[2].setLocation(halfWidth, halfHeight);
        vertices[3].setLocation(-halfWidth, halfHeight);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    protected void drawPrecise(Graphics2D graphics, AffineTransform transform) {
        if (preciseRect == null) {
            preciseRect = new Rectangle2D.Double(-width / 2.0, -height / 2.0, width, height);
        }

        Shape transformedShape = transform.createTransformedShape(preciseRect);

        if (filled) {
            graphics.fill(transformedShape);
        } else {
            graphics.draw(transformedShape);
        }
    }

}