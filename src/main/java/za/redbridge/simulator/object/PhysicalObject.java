package za.redbridge.simulator.object;

import java.awt.geom.RectangularShape;

import sim.util.Double2D;
import za.redbridge.simulator.portrayal.ShapePortrayal2D;

/**
 * Created by jamie on 2014/07/25.
 */
public abstract class PhysicalObject<T extends ShapePortrayal2D> {

    protected final double mass;
    protected final T portrayal;

    /** Position in MASON units */
    protected Double2D position;

    /** Velocity in MASON units per step */
    protected Double2D velocity;

    /** The forward vector of this object */
    protected Double2D forward;

    private RectangularShape collisionShape;

    public PhysicalObject(double mass, T portrayal) {
        this(mass, portrayal, new Double2D());
    }

    public PhysicalObject(double mass, T portrayal, Double2D position) {
        this(mass, portrayal, position, new Double2D());
    }

    public PhysicalObject(double mass, T portrayal, Double2D position, Double2D forward) {
        this(mass, portrayal, position, forward, new Double2D());
    }

    public PhysicalObject(double mass, T portrayal, Double2D position, Double2D forward,
            Double2D velocity) {
        this.mass = mass;
        this.portrayal = portrayal;

        this.position = position;
        this.forward = forward;
        this.velocity = velocity;

        this.collisionShape = createCollisionShape();
    }

    protected abstract RectangularShape createCollisionShape();

    public double getMass() {
        return mass;
    }

    public T getPortrayal() {
        return portrayal;
    }

    public Double2D getPosition() {
        return position;
    }

    public Double2D getVelocity() {
        return velocity;
    }

    public Double2D getForward() {
        return forward;
    }

    public RectangularShape getCollisionShape() {
        double x = position.x - collisionShape.getWidth() / 2;
        double y = position.y - collisionShape.getHeight() / 2;
        collisionShape.setFrame(x, y, collisionShape.getWidth(), collisionShape.getHeight());

        return collisionShape;
    }

}
