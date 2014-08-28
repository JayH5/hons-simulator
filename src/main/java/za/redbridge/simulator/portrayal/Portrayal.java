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
public abstract class Portrayal extends SimplePortrayal2D implements Drawable {

    protected boolean filled;
    protected Paint paint;

    protected float orientation;
    private boolean orientationChanged = true;

    protected final Rectangle2D draw = new Rectangle2D.Double();
    private boolean drawChanged = true;

    private Drawable childDrawable;

    private AffineTransform transformOverride;
    private AffineTransform transform = new AffineTransform();

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

    @Override
    public void setOrientation(float orientation) {
        if (orientation != this.orientation) {
            this.orientation = orientation;
            orientationChanged = true;

            if (childDrawable != null) {
                childDrawable.setOrientation(orientation);
            }
        }
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        Graphics2D g = (Graphics2D) graphics.create(); // glPushMatrix()

        drawChanged = !draw.equals(info.draw);
        if (drawChanged) {
            draw.setRect(info.draw);
        }

        if (needsTransform()) {
            transform.setToIdentity();
            transform.translate(info.draw.x, info.draw.y);
            transform.rotate(orientation);
            transform.scale(info.draw.width, info.draw.height);

            if (transformOverride != null) {
                transform.concatenate(transformOverride);
            }
        }

        g.setPaint(paint);

        if (info.precise) {
            drawPrecise(g, transform);
        } else {
            drawImprecise(g, transform);
        }

        if (childDrawable != null) {
            childDrawable.draw(object, g, info);
        }

        drawChanged = false;
        orientationChanged = false;

        g.dispose(); // glPopMatrix()
    }

    protected abstract void drawPrecise(Graphics2D graphics, AffineTransform transform);

    protected abstract void drawImprecise(Graphics2D graphics, AffineTransform transform);

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

    public void setChildDrawable(Drawable drawable) {
        this.childDrawable = drawable;
    }

    public AffineTransform getTransformOverride() {
        return transformOverride;
    }

    /**
     * Set a transform to override the orientation, scale and translation.
     * Set to null to clear.
     * @param transformOverride The transform to apply before drawing
     */
    public void setTransformOverride(AffineTransform transformOverride) {
        this.transformOverride = transformOverride;
    }

    /**
     * Check if this Portrayal has had its scale, rotation or translation changed since the last
     * draw call.
     * @return true if the Portrayal's transform has changed
     */
    protected boolean needsTransform() {
        return orientationChanged || drawChanged;
    }
}