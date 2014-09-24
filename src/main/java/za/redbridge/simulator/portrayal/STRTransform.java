package za.redbridge.simulator.portrayal;

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

import java.awt.geom.AffineTransform;

/**
 * Created by jamie on 2014/08/29.
 */
public class STRTransform {
    private final Transform transform;
    private float scaleX;
    private float scaleY;

    private AffineTransform affineTransform;

    private final Vec2 pool = new Vec2();

    public STRTransform(Transform transform, float scaleX, float scaleY) {
        this.transform = transform;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public STRTransform(Transform transform) {
        this(transform, 1, 1);
    }

    public STRTransform() {
        this(new Transform(), 1, 1);
    }

    public void setTransform(Transform transform) {
        this.transform.set(transform);
    }

    public Transform getTransform() {
        return transform;
    }

    public void setScale(float sx, float sy) {
        this.scaleX = sx;
        this.scaleY = sy;
    }

    public void transformVertices(Vec2[] vertices, int[] xPoints, int[] yPoints) {
        final int nVertices = vertices.length;
        if (nVertices != xPoints.length || nVertices != yPoints.length) {
            throw new IllegalArgumentException("Vertices and points have different lengths");
        }

        final Vec2 v = this.pool;
        for (int i = 0; i < nVertices; i++) {
            transformVertex(vertices[i], v);

            // Convert to int
            xPoints[i] = (int) v.x;
            yPoints[i] = (int) v.y;
        }
    }

    public void transformVertex(Vec2 vertex, Vec2 vOut) {
        vOut.set(vertex);

        // Transform (translate + rotate)
        Transform.mulToOut(transform, vOut, vOut);

        // Scale
        vOut.x *= scaleX;
        vOut.y *= scaleY;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getTranslationX() {
        return transform.p.x;
    }

    public float getTranslationY() {
        return transform.p.y;
    }

    public AffineTransform getAffineTransform() {
        if (affineTransform == null) {
            affineTransform = new AffineTransform();
        }

        affineTransform.setToIdentity();
        affineTransform.scale(scaleX, scaleY);
        affineTransform.translate(transform.p.x, transform.p.y);
        affineTransform.rotate(transform.q.getAngle());

        return affineTransform;
    }

    public static void mulToOut(STRTransform A, STRTransform B, STRTransform out) {
        Transform.mulToOut(A.transform, B.transform, out.transform);
        out.scaleX = A.scaleX * B.scaleX;
        out.scaleY = A.scaleY * B.scaleY;
    }
}
