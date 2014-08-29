package za.redbridge.simulator.portrayal;

import org.jbox2d.common.Vec2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 * Base class for drawing polygon shapes. Subclasses should set the positions of the vertices of
 * their shapes in their constructors.
 * Created by jamie on 2014/08/28.
 */
public abstract class PolygonPortrayal extends Portrayal {

    protected final Vec2[] vertices;

    private final int[] xPoints;
    private final int[] yPoints;

    protected final int nVertices;

    public PolygonPortrayal(int nVertices) {
        this(nVertices, Color.BLACK, true);
    }

    public PolygonPortrayal(int nVertices, Paint paint, boolean filled) {
        super(paint, filled);

        this.nVertices = nVertices;

        vertices = new Vec2[nVertices];
        for (int i = 0; i < nVertices; i++) {
            vertices[i] = new Vec2();
        }

        xPoints = new int[nVertices];
        yPoints = new int[nVertices];
    }

    @Override
    protected void drawPrecise(Graphics2D graphics, STRTransform transform,
            boolean transformUpdated) {
        drawImprecise(graphics, transform, transformUpdated);
    }

    @Override
    protected void drawImprecise(Graphics2D graphics, STRTransform transform,
            boolean transformUpdated) {
        if (transformUpdated) {
            transform.transformVertices(vertices, xPoints, yPoints);
        }

        if (filled) {
            graphics.fillPolygon(xPoints, yPoints, nVertices);
        } else {
            graphics.drawPolygon(xPoints, yPoints, nVertices);
        }
    }

}
