package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;

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
    protected void drawPrecise(Graphics2D graphics) {
        if (preciseEllipse == null) {
            preciseEllipse = new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2);
        }

        if (filled) {
            graphics.fill(preciseEllipse);
        } else {
            graphics.draw(preciseEllipse);
        }
    }

    @Override
    protected void drawImprecise(Graphics2D graphics) {
        int drawWidth = (int) (radius * 2);
        int drawHeight = (int) (radius * 2);
        int x = (int) -radius;
        int y = (int) -radius;

        if (filled) {
            graphics.fillOval(x, y, drawWidth, drawHeight);
        } else {
            graphics.drawOval(x, y, drawWidth, drawHeight);
        }
    }

}