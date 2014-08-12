package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;

/**
 * Created by jamie on 2014/08/07.
 */
public class SensorPortrayal extends Portrayal {

    private final Polygon triangle;

    public SensorPortrayal(double range, double fieldOfView, double orientation, Paint paint) {
        super(paint, true);

        double theta = fieldOfView / 2.0;
        int dx = (int) (range * Math.cos(theta));
        int dy = (int) (range * Math.sin(theta));

        triangle = new Polygon(new int[]{ 0, dx, dx }, new int[]{ 0, dy, -dy }, 3);
    }

    @Override
    protected void drawPrecise(Graphics2D graphics) {
        // No precise drawing available
        drawImprecise(graphics);
    }

    @Override
    protected void drawImprecise(Graphics2D graphics) {
        if (filled) {
            graphics.fillPolygon(triangle);
        } else {
            graphics.drawPolygon(triangle);
        }
    }
}
