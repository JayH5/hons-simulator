package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Provides a simple oval portrayal of an object.
 *
 * Created by jamie on 2014/07/23.
 */
public class CirclePortrayal extends Portrayal {

    private final double radius;

    private transient Ellipse2D.Double preciseEllipse;

    public CirclePortrayal(double radius) {
        super();
        this.radius = radius;
    }

    public CirclePortrayal(double radius, Paint paint, boolean filled) {
        super(paint, filled);
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    protected void drawPrecise(Graphics2D graphics, Rectangle2D draw, float rotation) {
        if (preciseEllipse == null) {
            preciseEllipse = new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2);
        }

        Shape transformedShape = getTransform().createTransformedShape(preciseEllipse);

        if (filled) {
            graphics.fill(transformedShape);
        } else {
            graphics.draw(transformedShape);
        }
    }

    @Override
    protected void drawImprecise(Graphics2D graphics, Rectangle2D draw, float rotation) {
        int drawWidth = (int) (draw.getWidth() * radius * 2);
        int drawHeight = (int) (draw.getHeight() * radius * 2);
        int x = (int) (-drawWidth / 2.0 + draw.getX());
        int y = (int) (-drawHeight / 2.0 + draw.getY());

        if (filled) {
            graphics.fillOval(x, y, drawWidth, drawHeight);
        } else {
            graphics.drawOval(x, y, drawWidth, drawHeight);
        }
    }

}