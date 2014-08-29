package za.redbridge.simulator.portrayal;

import org.jbox2d.common.Transform;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;

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
 * NOTE: Does not implement more specific versions of {@link SimplePortrayal2D#hitObject(Object,
 * DrawInfo2D)} or {@link SimplePortrayal2D#handleMouseEvent(GUIState, Manipulating2D,
 * LocationWrapper, MouseEvent, DrawInfo2D, int)}.
 *
 * Created by jamie on 2014/07/23.
 */
public abstract class Portrayal extends SimplePortrayal2D implements Drawable {

    protected boolean filled;
    protected Paint paint;

    private final STRTransform transform = new STRTransform();
    private boolean transformUpdated = true;

    private Drawable childDrawable;

    private STRTransform localTransform;
    private STRTransform effectiveLocalTransform;
    private boolean hasLocalTransform = false;

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
    public void setTransform(Transform transform) {
        this.transform.setTransform(transform);
        if (childDrawable != null) {
            childDrawable.setTransform(transform);
        }

        transformUpdated = true;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        Graphics2D g = (Graphics2D) graphics.create(); // glPushMatrix()

        g.setPaint(paint);

        updateScale(info);

        if (hasLocalTransform) {
            drawLocal(object, g, info);
        } else {
            if (info.precise) {
                drawPrecise(g, transform, transformUpdated);
            } else {
                drawImprecise(g, transform, transformUpdated);
            }
        }

        if (childDrawable != null) {
            childDrawable.draw(object, g, info);
        }

        g.dispose(); // glPopMatrix()

        transformUpdated = false;
    }

    private void drawLocal(Object object, Graphics2D graphics, DrawInfo2D info) {
        STRTransform.mulToOut(transform, localTransform, effectiveLocalTransform);

        if (info.precise) {
            drawPrecise(graphics, effectiveLocalTransform, transformUpdated);
        } else {
            drawImprecise(graphics, effectiveLocalTransform, transformUpdated);
        }

        if (childDrawable != null) {
            childDrawable.draw(object, graphics, info);
        }
    }

    private void updateScale(DrawInfo2D info) {
        float sx = (float) info.draw.width;
        float sy = (float) info.draw.height;
        if (sx != transform.getScaleX() || sy != transform.getScaleY()) {
            transform.setScale(sx, sy);
            transformUpdated = true;
        }
    }

    protected abstract void drawPrecise(Graphics2D graphics, STRTransform transform,
            boolean transformUpdated);

    protected abstract void drawImprecise(Graphics2D graphics, STRTransform transform,
            boolean transformUpdated);

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

    public void setLocalTransform(STRTransform localTransform) {
        this.localTransform = localTransform;

        if (effectiveLocalTransform == null) {
            effectiveLocalTransform = new STRTransform();
        }
        STRTransform.mulToOut(transform, localTransform, effectiveLocalTransform);

        hasLocalTransform = true;
    }

    public void clearLocalTransform() {
        hasLocalTransform = false;
    }

}