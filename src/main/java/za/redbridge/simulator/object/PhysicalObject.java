package za.redbridge.simulator.object;

import java.awt.geom.RectangularShape;

import sim.util.Double2D;
import za.redbridge.simulator.portrayal.ShapePortrayal2D;

/**
 * Created by jamie on 2014/07/25.
 */
public abstract class PhysicalObject {

    private static final double EPSILON = 0.0001;

    private final double mass;
    private final ShapePortrayal2D portrayal;

    /** Position in MASON units */
    private Double2D position;
    private Double2D previousPosition;

    /** Velocity in MASON units per step */
    private Double2D velocity;
    private Double2D previousVelocity;

    /** The forward vector of this object */
    private Double2D forward;

    private RectangularShape collisionShape;

    public PhysicalObject(double mass, ShapePortrayal2D portrayal) {
        this(mass, portrayal, new Double2D());
    }

    public PhysicalObject(double mass, ShapePortrayal2D portrayal, Double2D position) {
        this(mass, portrayal, position, new Double2D());
    }

    public PhysicalObject(double mass, ShapePortrayal2D portrayal, Double2D position,
            Double2D forward) {
        this(mass, portrayal, position, forward, new Double2D());
    }

    public PhysicalObject(double mass, ShapePortrayal2D portrayal, Double2D position,
            Double2D forward, Double2D velocity) {
        this.mass = mass;
        this.portrayal = portrayal;

        this.position = position;
        this.forward = forward;
        this.velocity = velocity;

        this.collisionShape = createCollisionShape();
        this.previousPosition = new Double2D();
    }

    protected abstract RectangularShape createCollisionShape();

    public double getMass() {
        return mass;
    }

    public ShapePortrayal2D getPortrayal() {
        return portrayal;
    }

    public Double2D getPosition() {
        return position;
    }

    public boolean positionChanged() {
        return Math.abs(position.x - previousPosition.x) >= EPSILON
                || Math.abs(position.y - previousPosition.y) >= EPSILON;
    }

    public Double2D getVelocity() {
        return velocity;
    }

    public Double2D getForward() {
        return forward;
    }

    /**
     * Applies the force to the object. This updates the object's velocity and position by
     * calculating the acceleration due to the force based on the object's mass. This method assumes
     * that all other forces have been resolved on the object (e.g. friction, collision forces, etc)
     * and simply applies this force to the object.
     * @param force The nett calculated force on this object.
     */
    public void applyNettForce(Double2D force) {
        double accelerationX = force.x / mass;
        double accelerationY = force.y / mass;

        Double2D newVelocity = new Double2D(velocity.x + accelerationX, velocity.y + accelerationY);
        Double2D newPosition = new Double2D(position.x + newVelocity.x, position.y + newVelocity.y);

        previousVelocity = velocity;
        velocity = newVelocity;

        previousPosition = position;
        position = newPosition;
    }

    /**
     * Reverts the effects of the last force that was applied to the object. Returns the position
     * and velocity values to their previous values. Hacky collision response.
     */
    public void revertAppliedForce() {
        position = previousPosition;
        velocity = previousVelocity;
    }

    /**
     * Get the bounding shape for this object.
     * @return Some instance of an implementation of {@link RectangularShape}
     */
    public RectangularShape getCollisionShape() {
        double x = position.x - collisionShape.getWidth() / 2;
        double y = position.y - collisionShape.getHeight() / 2;
        collisionShape.setFrame(x, y, collisionShape.getWidth(), collisionShape.getHeight());

        return collisionShape;
    }

}
