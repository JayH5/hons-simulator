package za.redbridge.simulator.portrayal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimpleInspector;
import sim.portrayal.SimplePortrayal2D;

/**
 * Base class for all our portrayal objects.
 *
 * NOTE: Does implement more specific versions of {@link SimplePortrayal2D#hitObject(Object,
 * DrawInfo2D)} or {@link SimplePortrayal2D#handleMouseEvent(GUIState, Manipulating2D,
 * LocationWrapper, MouseEvent, DrawInfo2D, int)}.
 *
 * Created by jamie on 2014/07/23.
 */
public abstract class Portrayal extends SimplePortrayal2D {

    protected boolean filled;
    protected Paint paint;

    private double orientation;

    public Portrayal() {
        this(Color.BLACK, true);
    }

    public Portrayal(Paint paint, boolean filled) {
        this.paint = paint;
        this.filled = filled;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        Graphics2D g = (Graphics2D) graphics.create(); // glPushMatrix()

        g.translate(info.draw.x, info.draw.y);
        g.rotate(orientation);
        g.scale(info.draw.width, info.draw.height);

        g.setPaint(paint);

        if (info.precise) {
            drawPrecise(g);
        } else {
            drawImprecise(g);
        }

        g.dispose(); // glPopMatrix()
    }

    protected abstract void drawPrecise(Graphics2D graphics);

    protected abstract void drawImprecise(Graphics2D graphics);

    @Override
    public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
        if (wrapper == null) {
            return null;
        }
        return new SimpleInspector(wrapper.getObject(), state, "Properties");
    }

    @Override
    public String getName(LocationWrapper wrapper) {
        if (wrapper == null) {
            return "";
        }
        return String.valueOf(wrapper.getObject());
    }

    @Override
    public String getStatus(LocationWrapper wrapper) {
        return getName(wrapper);
    }

    @Override
    public boolean setSelected(LocationWrapper wrapper, boolean selected) {
        return false;
    }

}