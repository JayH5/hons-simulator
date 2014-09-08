package za.redbridge.simulator.portrayal;

import org.jbox2d.common.Vec2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;

import sim.util.Double2D;

import static za.redbridge.simulator.Utils.toVec2;

/**
 * Created by jamie on 2014/09/04.
 */
public class LinePortrayal extends Portrayal {

    private final Vec2 v1;
    private final Vec2 v2;

    private final Vec2 v1Draw = new Vec2();
    private final Vec2 v2Draw = new Vec2();

    private int x1;
    private int y1;
    private int x2;
    private int y2;

    private transient Line2D preciseLine;

    public LinePortrayal(Double2D v1, Double2D v2) {
        this(v1, v2, Color.BLACK, true);
    }

    public LinePortrayal(Double2D v1, Double2D v2, Paint paint, boolean filled) {
        super(paint, filled);
        this.v1 = toVec2(v1);
        this.v2 = toVec2(v2);
    }

    @Override
    protected void drawPrecise(Graphics2D graphics, STRTransform transform,
            boolean transformUpdated) {
        if (preciseLine == null) {
            preciseLine = new Line2D.Double(v1.x, v1.y, v2.x, v2.y);
        }

        Shape shape = transform.getAffineTransform().createTransformedShape(preciseLine);

        graphics.draw(shape);
    }

    @Override
    protected void drawImprecise(Graphics2D graphics, STRTransform transform,
        boolean transformUpdated) {
        if (transformUpdated) {
            transform.transformVertex(v1, v1Draw);
            transform.transformVertex(v2, v2Draw);

            x1 = (int) v1Draw.x;
            y1 = (int) v1Draw.y;
            x2 = (int) v2Draw.x;
            y2 = (int) v2Draw.y;
        }

        graphics.drawLine(x1, y1, x2, y2);
    }
}
