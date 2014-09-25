package za.redbridge.simulator.sensor.sensedobjects;

import java.awt.geom.Line2D;

import za.redbridge.simulator.object.PhysicalObject;

/**
 * A SensedObject container for an object with a {@link org.jbox2d.collision.shapes.EdgeShape}
 * Fixture shape. Describes the line "seen" by the sensor.
 * Created by jamie on 2014/09/05.
 */
public class EdgeSensedObject extends SensedObject<Line2D> {

    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;

    private Line2D shape;

    public EdgeSensedObject(PhysicalObject object, float distance, float x0, float y0, float x1,
                float y1) {
        super(object, distance);

        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    @Override
    public Line2D getShape() {
        if (shape == null) {
            shape = new Line2D.Float(x0, y0, x1, y1);
        }

        return shape;
    }
}
