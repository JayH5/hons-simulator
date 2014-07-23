package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by jamie on 2014/07/23.
 */
public class OvalPortrayal2D extends ShapePortrayal2D {

    private final double width;
    private final double height;

    private transient Ellipse2D.Double preciseEllipse;

    public OvalPortrayal2D(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    protected void drawPrecise(Graphics2D graphics, Rectangle2D.Double drawRect) {
        if (preciseEllipse == null) {
            preciseEllipse = new Ellipse2D.Double();
        }

        double drawWidth = width * drawRect.width;
        double drawHeight = height * drawRect.height;
        double x = drawRect.x - drawWidth / 2;
        double y = drawRect.y - drawHeight / 2;

        preciseEllipse.setFrame(x, y, drawWidth, drawHeight);
        if (filled) {
            graphics.fill(preciseEllipse);
        } else {
            graphics.draw(preciseEllipse);
        }
    }

    @Override
    protected void drawImprecise(Graphics2D graphics, Rectangle2D.Double drawRect) {
        int drawWidth = (int) (width * drawRect.width);
        int drawHeight = (int) (height * drawRect.height);
        int x = (int) (drawRect.x - drawWidth / 2.0);
        int y = (int) (drawRect.y - drawHeight / 2.0);

        if (filled) {
            graphics.fillOval(x, y, drawWidth, drawHeight);
        } else {
            graphics.drawOval(x, y, drawWidth, drawHeight);
        }
    }
}
