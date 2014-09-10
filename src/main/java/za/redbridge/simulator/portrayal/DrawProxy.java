package za.redbridge.simulator.portrayal;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimpleInspector;
import sim.portrayal.SimplePortrayal2D;

/**
 * Proxy for all draw requests from MASON. Workaround for the fact that AWT's coordinate system
 * has it's origin at the top left corner. Inverts the y-axis.
 *
 * NOTE: Does not implement more specific versions of {@link SimplePortrayal2D#hitObject(Object,
 * DrawInfo2D)} or {@link SimplePortrayal2D#handleMouseEvent(GUIState, sim.display.Manipulating2D,
 * LocationWrapper, java.awt.event.MouseEvent, DrawInfo2D, int)}.
 *
 * Created by jamie on 2014/09/10.
 */
public class DrawProxy extends SimplePortrayal2D {

    private final List<Drawable> drawables = new ArrayList<>();

    private final AffineTransform transform = new AffineTransform();

    private final Rectangle2D lastDraw = new Rectangle2D.Double();

    private final double width;
    private final double height;

    public DrawProxy(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        Graphics2D g = (Graphics2D) graphics.create();

        if (!info.draw.equals(lastDraw)) {
            transform.setToIdentity();
            transform.translate(0, height * info.draw.height);
            transform.translate(info.draw.x, info.draw.y);
            transform.scale(1, -1);

            lastDraw.setRect(info.draw);
        }

        g.setTransform(transform);

        for (Drawable drawable : drawables) {
            drawable.draw(object, g, info);
        }

        g.dispose();
    }

    public void registerDrawable(Drawable drawable) {
        if (!drawables.contains(drawable)) {
            drawables.add(drawable);
        }
    }

    public void unregisterDrawable(Drawable drawable) {
        drawables.remove(drawable);
    }

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
