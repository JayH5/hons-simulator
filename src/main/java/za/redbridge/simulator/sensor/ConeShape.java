package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

/**
 * Shape to describe the sensor field.
 *
 * Created by jamie on 2014/12/05.
 */
public class ConeShape extends PolygonShape {

    private static final double ARC_SEGMENT_RADIANS = Math.PI / 8;

    public ConeShape(float length, float theta, Transform transform) {
        super();

        final int segments = (int) Math.ceil(theta / ARC_SEGMENT_RADIANS);

        final int nVertices = segments + 2;
        Vec2[] vertices = new Vec2[nVertices];
        vertices[0] = new Vec2();

        float segmentRadians = theta / segments;
        float angle = -theta / 2;
        for (int i = 1; i < nVertices; i++) {
            float x = (float) (length * Math.cos(angle));
            float y = (float) (length * Math.sin(angle));

            vertices[i] = new Vec2(x, y);

            angle += segmentRadians;
        }

        for (int i = 0; i < nVertices; i++) {
            Transform.mulToOut(transform, vertices[i], vertices[i]);
        }

        this.set(vertices, nVertices);
    }
}
