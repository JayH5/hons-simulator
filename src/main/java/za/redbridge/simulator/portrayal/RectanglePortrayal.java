package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

/**
 * Provides a simple portrayal of a rectangular object.
 *
 * Created by jamie on 2014/07/23.
 */
public class RectanglePortrayal extends Portrayal {

    private transient Rectangle2D.Double preciseRect;

    private final double width;
    private final double height;

    public RectanglePortrayal(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public RectanglePortrayal(double width, double height, Paint paint, boolean filled) {
        super(paint, filled);
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
    protected void drawPrecise(Graphics2D graphics) {
        if (preciseRect == null) {
            preciseRect = new Rectangle2D.Double(-width / 2.0, -height / 2.0, width, height);
        }

        if (filled) {
            graphics.fill(preciseRect);
        } else {
            graphics.draw(preciseRect);
        }
    }

    @Override
    protected void drawImprecise(Graphics2D graphics) {
        int drawWidth = (int) width;
        int drawHeight = (int) height;
        int x = -drawWidth / 2;
        int y = -drawHeight / 2;

        if (filled) {
            graphics.fillRect(x, y, drawWidth, drawHeight);
        } else {
            graphics.drawRect(x, y, drawWidth, drawHeight);
        }
    }
}