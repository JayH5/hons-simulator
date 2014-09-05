package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * Provides a simple oval portrayal of an object.
 *
 * Created by jamie on 2014/07/23.
 */
public class CirclePortrayal extends Portrayal {

    private final double radius;

    private transient Ellipse2D preciseEllipse;

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
    protected void drawPrecise(Graphics2D graphics, STRTransform transform,
            boolean transformUpdated) {
        if (preciseEllipse == null) {
            preciseEllipse = new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2);
        }

        Shape transformedShape =
                transform.getAffineTransform().createTransformedShape(preciseEllipse);

        if (filled) {
            graphics.fill(transformedShape);
        } else {
            graphics.draw(transformedShape);
        }
    }

    @Override
    protected void drawImprecise(Graphics2D graphics, STRTransform transform,
            boolean transformUpdated) {
        int x = (int) ((-radius + transform.getTranslationX()) * transform.getScaleX());
        int y = (int) ((-radius + transform.getTranslationY()) * transform.getScaleY());
        int width = (int) (radius * 2 * transform.getScaleX());
        int height = (int) (radius * 2 * transform.getScaleY());

        if (filled) {
            graphics.fillOval(x, y, width, height);
        } else {
            graphics.drawOval(x, y, width, height);
        }
    }

}