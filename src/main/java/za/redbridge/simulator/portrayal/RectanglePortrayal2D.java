package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by jamie on 2014/07/23.
 */
public class RectanglePortrayal2D extends ShapePortrayal2D {

    private transient Rectangle2D.Double preciseRect;

    private final double width;
    private final double height;

    public RectanglePortrayal2D(double width, double height) {
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
        if (preciseRect == null) {
            preciseRect = new Rectangle2D.Double();
        }

        double drawWidth = width * drawRect.width;
        double drawHeight = height * drawRect.height;
        double x = drawRect.x - drawWidth / 2;
        double y = drawRect.y - drawHeight / 2;

        preciseRect.setFrame(x, y, drawWidth, drawHeight);
        if (filled) {
            graphics.fill(preciseRect);
        } else {
            graphics.draw(preciseRect);
        }
    }

    @Override
    protected void drawImprecise(Graphics2D graphics, Rectangle2D.Double drawRect) {
        int drawWidth = (int) (width * drawRect.width);
        int drawHeight = (int) (height * drawRect.height);
        int x = (int) (drawRect.x - drawWidth / 2.0);
        int y = (int) (drawRect.y - drawHeight / 2.0);

        if (filled) {
            graphics.fillRect(x, y, drawWidth, drawHeight);
        } else {
            graphics.drawRect(x, y, drawWidth, drawHeight);
        }
    }
}
