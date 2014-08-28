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

    private float rotation;
    private boolean rotationChanged = true;

    private final Rectangle2D draw = new Rectangle2D.Double();
    private boolean drawChanged = true;

    private Drawable childDrawable;

    private float localRotation;
    private Rectangle2D localDraw;
    private AffineTransform localTransform;
    private Rectangle2D effectiveLocalDraw;
    private boolean hasLocalTransform = false;

    private AffineTransform transform;

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
    public void setRotation(float rotation) {
        if (rotation != this.rotation) {
            this.rotation = rotation;
            rotationChanged = true;

            if (childDrawable != null) {
                childDrawable.setRotation(rotation);
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

        g.setPaint(paint);

        if (hasLocalTransform) {
            drawLocal(object, g, info);
        } else {
            if (info.precise) {
                drawPrecise(g, draw, rotation);
            } else {
                drawImprecise(g, draw, rotation);
            }
        }

        if (childDrawable != null) {
            childDrawable.draw(object, g, info);
        }

        drawChanged = false;
        rotationChanged = false;

        g.dispose(); // glPopMatrix()
    }

    private void drawLocal(Object object, Graphics2D graphics, DrawInfo2D info) {
        if (drawChanged) {
            effectiveLocalDraw.setRect(
                    draw.getX() + localDraw.getX(), // Translate
                    draw.getY() + localDraw.getY(),
                    draw.getWidth() * localDraw.getWidth(), // Scale
                    draw.getHeight() * localDraw.getHeight());
        }

        final Rectangle2D draw = effectiveLocalDraw;
        final float rotation = this.rotation + this.localRotation;

        if (info.precise) {
            drawPrecise(graphics, draw, rotation);
        } else {
            drawImprecise(graphics, draw, rotation);
        }

        if (childDrawable != null) {
            childDrawable.draw(object, graphics, info);
        }
    }

    protected abstract void drawPrecise(Graphics2D graphics, Rectangle2D draw, float rotation);

    protected abstract void drawImprecise(Graphics2D graphics, Rectangle2D draw, float rotation);

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

    protected AffineTransform getTransform() {
        if (transform == null) {
            transform = new AffineTransform();
        }

        if (needsTransform()) {
            transform.setToIdentity();
            transform.translate(draw.getX(), draw.getY());
            transform.rotate(rotation);
            transform.scale(draw.getWidth(), draw.getHeight());

            if (hasLocalTransform) {
                transform.concatenate(localTransform);
            }
        }
        return transform;
    }

    public void setLocalTransform(Rectangle2D draw, float orientation) {
        this.localDraw = draw;
        this.localRotation = orientation;
        effectiveLocalDraw = new Rectangle2D.Double();
        hasLocalTransform = true;
        buildExtraTransform();
    }

    public void clearLocalTransform() {
        hasLocalTransform = false;
    }

    private void buildExtraTransform() {
        if (localTransform == null) {
            localTransform = new AffineTransform();
        }
        localTransform.setToIdentity();
        localTransform.translate(localDraw.getX(), localDraw.getY());
        localTransform.rotate(localRotation);
        localTransform.scale(localDraw.getWidth(), localDraw.getHeight());
    }

    /**
     * Check if this Portrayal has had its scale, rotation or translation changed since the last
     * draw call.
     * @return true if the Portrayal's transform has changed
     */
    protected boolean needsTransform() {
        return rotationChanged || drawChanged;
    }
}