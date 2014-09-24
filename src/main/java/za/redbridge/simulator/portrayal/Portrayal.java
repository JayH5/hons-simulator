package za.redbridge.simulator.portrayal;

import org.jbox2d.common.Transform;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;

import sim.portrayal.DrawInfo2D;

/**
 * Base class for all our portrayal objects.
 *
 * Created by jamie on 2014/07/23.
 */
public abstract class Portrayal implements Drawable {

    protected boolean filled;
    protected Paint paint;

    private final STRTransform transform = new STRTransform();
    private boolean transformUpdated = true;

    private Drawable childDrawable;

    private STRTransform localTransform;
    private STRTransform effectiveLocalTransform;
    private boolean hasLocalTransform = false;

    private boolean enabled = true;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        if (!enabled) {
            return;
        }

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

    public void setChildDrawable(Drawable drawable) {
        this.childDrawable = drawable;
        if (childDrawable != null) {
            childDrawable.setTransform(transform.getTransform());
        }
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