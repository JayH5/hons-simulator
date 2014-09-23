package za.redbridge.simulator.sensor.sensedobjects;

import java.awt.geom.Arc2D;

import za.redbridge.simulator.object.PhysicalObject;

/**
 * A SensedObject container for an object with a {@link org.jbox2d.collision.shapes.CircleShape}
 * Fixture shape. Describes the arc of the circle "seen" by the sensor.
 * Created by jamie on 2014/09/05.
 */
public class CircleSensedObject extends SensedObject<Arc2D> {

    private final float radius;
    private final float x;
    private final float y;
    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;

    private Arc2D shape;

    public CircleSensedObject(PhysicalObject object, float distance, float radius,
            float x, float y, float x0, float y0, float x1, float y1) {
        super(object, distance);
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    @Override
    public Arc2D getShape() {
        if (shape == null) {
            double angSt = Math.atan2(y0 - y, x0 - x);
            double angExt = -(Math.atan2(y1 - y, x1 - x) - angSt);

            shape = new Arc2D.Float();
            shape.setArcByCenter(x, y, radius, angSt, angExt, Arc2D.OPEN);
        }
        return shape;
    }

}
