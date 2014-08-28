package za.redbridge.simulator.portrayal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Base class for drawing polygon shapes. Subclasses should set the positions of the vertices of
 * their shapes in their constructors.
 * Created by jamie on 2014/08/28.
 */
public abstract class PolygonPortrayal extends Portrayal {

    protected final Point2D[] vertices;
    private final Point2D[] drawVertices;

    private final int[] xPoints;
    private final int[] yPoints;

    protected final int nVertices;

    public PolygonPortrayal(int nVertices) {
        this(nVertices, Color.BLACK, true);
    }

    public PolygonPortrayal(int nVertices, Paint paint, boolean filled) {
        super(paint, filled);

        this.nVertices = nVertices;

        vertices = new Point2D[nVertices];
        drawVertices = new Point2D[nVertices];
        for (int i = 0; i < nVertices; i++) {
            vertices[i] = new Point2D.Double();
            drawVertices[i] = new Point2D.Double();
        }

        xPoints = new int[nVertices];
        yPoints = new int[nVertices];
    }

    @Override
    protected void drawPrecise(Graphics2D graphics, Rectangle2D draw, float orientation) {
        drawImprecise(graphics, draw, orientation);
    }

    @Override
    protected void drawImprecise(Graphics2D graphics, Rectangle2D draw, float orientation) {
        if (needsTransform()) {
            getTransform().transform(vertices, 0, drawVertices, 0, nVertices);
            for (int i = 0; i < nVertices; i++) {
                xPoints[i] = (int) drawVertices[i].getX();
                yPoints[i] = (int) drawVertices[i].getY();
            }
        }

        if (filled) {
            graphics.fillPolygon(xPoints, yPoints, nVertices);
        } else {
            graphics.drawPolygon(xPoints, yPoints, nVertices);
        }
    }

}
