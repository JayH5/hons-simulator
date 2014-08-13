package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;

import sim.portrayal.DrawInfo2D;

/**
 * Created by jamie on 2014/08/07.
 */
public class SensorPortrayal extends Portrayal {

    private final Polygon triangle;

    private final double bearing;
    private final double orientation;

    private double offsetX;
    private double offsetY;

    public SensorPortrayal(double bearing, double orientation, double range, double fieldOfView,
            Paint paint) {
        super(paint, true);

        this.bearing = bearing;
        this.orientation = orientation;

        int dx = (int) (range * Math.cos(fieldOfView / 2));
        int dy = (int) (range * Math.sin(fieldOfView / 2));

        triangle = new Polygon(new int[] { 0, dx, dx }, new int[] { 0, dy, -dy }, 3);
    }

    public void setRobotRadius(double robotRadius) {
        offsetX = robotRadius * Math.cos(bearing);
        offsetY = robotRadius * Math.sin(bearing);
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        // Don't call super, have our own rotation, translation scheme
        Graphics2D g = (Graphics2D) graphics.create(); // glPushMatrix()

        g.translate(offsetX, offsetY);
        g.rotate(bearing + orientation);

        g.setPaint(paint);
        drawImprecise(g);

        g.dispose();
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
