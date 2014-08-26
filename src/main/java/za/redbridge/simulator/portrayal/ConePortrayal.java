package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;

/**
 * Created by jamie on 2014/08/07.
 */
public class ConePortrayal extends Portrayal {

    private static final double ARC_SEGMENT_RADIANS = Math.PI / 16;

    protected final Polygon polygon;

    public ConePortrayal(double length, double theta, Paint paint) {
        super(paint, true);

        int segments = (int) Math.ceil(theta / ARC_SEGMENT_RADIANS);
        int vertices = segments + 3; // Min 4 points
        int[] xVertices = new int[vertices];
        int[] yVertices = new int[vertices];

        xVertices[0] = 0;
        yVertices[0] = 0;

        double segmentRadians = theta / segments;
        double angle = -theta / 2;
        for (int i = 0; i <= segments; i++) {
            xVertices[i + 1] = (int) (length * Math.cos(angle));
            yVertices[i + 1] = (int) (length * Math.sin(angle));
            angle += segmentRadians;
        }

        polygon = new Polygon(xVertices, yVertices, vertices);
    }

    @Override
    protected void drawPrecise(Graphics2D graphics) {
        // No precise drawing available
        drawImprecise(graphics);
    }

    @Override
    protected void drawImprecise(Graphics2D graphics) {
        if (filled) {
            graphics.fillPolygon(polygon);
        } else {
            graphics.drawPolygon(polygon);
        }
    }
}
