package za.redbridge.simulator.portrayal;

import org.jbox2d.common.Transform;

import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;

/**
 * Interface for drawing child objects relative to other drawn objects
 * Created by jamie on 2014/08/26.
 */
public interface Drawable {

    /**
     * This method will be called after the parent object has drawn itself but before it has
     * disposed of its graphics context. Thus, this enables the drawing of child objects relative to
     * a parent object.
     * @param object the root portrayal object to be drawn
     * @param graphics the translated, rotated and scaled graphics context
     * @param info the draw info from MASON
     */
    void draw(Object object, Graphics2D graphics, DrawInfo2D info);

    void setTransform(Transform transform);
}
