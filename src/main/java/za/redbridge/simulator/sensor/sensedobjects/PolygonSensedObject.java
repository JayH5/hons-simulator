package za.redbridge.simulator.sensor.sensedobjects;

import java.awt.geom.Rectangle2D;

import za.redbridge.simulator.object.PhysicalObject;

/**
 * A SensedObject container for an object with a {@link org.jbox2d.collision.shapes.PolygonShape}
 * Fixture shape. Polygon's are tricky to describe so we merely describe the bounding box of the
 * shape relative to the sensor within the sensor's field of view.
 * Created by jamie on 2014/09/05.
 */
public class PolygonSensedObject extends SensedObject<Rectangle2D> {

    private final float x;
    private final float y;
    private final float w;
    private final float h;

    private Rectangle2D shape;

    public PolygonSensedObject(PhysicalObject object, double distance, float x, float y, float w, float h) {
        super(object, distance);

        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    public Rectangle2D getShape() {
        if (shape == null) {
            shape = new Rectangle2D.Float(x, y, w, h);
        }

        return shape;
    }
}
