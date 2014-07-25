package za.redbridge.simulator;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import za.redbridge.simulator.object.PhysicalObject;

/**
 * Created by jamie on 2014/07/25.
 */
public class PhysicsEngine implements Steppable {

    private static final double MAX_OBJECT_RADIUS = 4.0;
    private static final Double2D ZERO_VECTOR = new Double2D();

    // Nice to have
    private static final double FORCE_GRAVITY = 9.8;
    private static final double COEFFICIENT_OF_FRICTION_STATIC = 0.5;
    private static final double COEFFICIENT_OF_FRICTION_DYNAMIC = 0.3;

    private final List<PhysicalObject> objects = new ArrayList<>();

    public PhysicsEngine() {

    }

    public void addObject(PhysicalObject object) {
        if (objects.contains(object)) {
            throw new IllegalArgumentException("Object already added");
        }

        objects.add(object);
    }

    @Override
    public void step(SimState state) {
        Simulation simulation = (Simulation) state;
        Continuous2D environment = simulation.getEnvironment();

        // Calculate the nett force on each object to update its position
        for (PhysicalObject obj : objects) {
            Double2D nettForce = calculateNettForce(obj, environment);
            obj.applyNettForce(nettForce);
        }

        // Check for collisions between objects
        for (PhysicalObject obj : objects) {
            if (isColliding(obj, environment)) {
                obj.revertAppliedForce();
            }
        }

        // Update the positions of all the objects in the
        for (PhysicalObject obj : objects) {
            if (obj.positionChanged()) {
                environment.setObjectLocation(obj.getPortrayal(), obj.getPosition());
            }
        }
    }

    /* Calculate the nett force on the provided object. */
    private Double2D calculateNettForce(PhysicalObject obj, Continuous2D environment) {
        // TODO
        return ZERO_VECTOR;
    }

    private boolean isColliding(PhysicalObject obj, Continuous2D environment) {
        RectangularShape collisionShape = obj.getCollisionShape();

        if (isCollidingWithWall(collisionShape, environment)) {
            return true;
        }

        Double2D position = obj.getPosition();
        double distance =
                Math.max(collisionShape.getX(), collisionShape.getY()) + MAX_OBJECT_RADIUS;

        Bag neighbours = environment.getNeighborsWithinDistance(position, distance);
        for (Object neighbour : neighbours) {
            // Only know what to do with physical objects
            if (neighbour instanceof PhysicalObject) {
                // Get the other object's collision shape
                PhysicalObject otherObject = (PhysicalObject) neighbour;
                RectangularShape otherShape = otherObject.getCollisionShape();

                // Do a rough bounds check first
                if (collisionShape.getBounds().intersects(otherShape.getBounds())) {
                    // Finally, do an accurate check
                    if (intersects(collisionShape, otherShape)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isCollidingWithWall(RectangularShape shape, Continuous2D environment) {
        return shape.getMinX() <= 0 || shape.getMaxX() >= environment.getWidth()
                && shape.getMinY() <= 0 || shape.getMaxY() >= environment.getHeight();
    }

    private boolean intersects(RectangularShape shape1, RectangularShape shape2) {
        if (shape1 instanceof Ellipse2D) {
            if (shape2 instanceof Rectangle2D) {
                return shape1.intersects((Rectangle2D) shape2);
            }
            if (shape2 instanceof Ellipse2D) {
                return ellipsesIntersect((Ellipse2D) shape1, (Ellipse2D) shape2);
            }
        }
        if (shape2 instanceof  Ellipse2D) {
            if (shape1 instanceof Rectangle2D) {
                return shape1.intersects((Rectangle2D) shape2);
            }
        }

        if (shape1 instanceof Rectangle2D && shape2 instanceof Rectangle2D) {
            return shape1.intersects((Rectangle2D) shape2);
        }
        return false;
    }

    private boolean ellipsesIntersect(Ellipse2D ellipse1, Ellipse2D ellipse2) {
        Rectangle bounds1 = ellipse1.getBounds();
        Rectangle bounds2 = ellipse2.getBounds();

        // If the ellipses are (roughly) round then we can do circle intersection
        if (bounds1.getWidth() == bounds1.getHeight()
                && bounds2.getWidth() == bounds2.getHeight()) {
            return circlesIntersect(ellipse1, ellipse2);
        }

        // Otherwise we just do best effort
        return bounds1.intersects(bounds2);
    }

    private boolean circlesIntersect(Ellipse2D ellipse1, Ellipse2D ellipse2) {
        double radius1 = ellipse1.getWidth() / 2;
        double radius2 = ellipse2.getWidth() / 2;

        double minDistance = radius1 + radius2;
        double minDistanceSquared = minDistance * minDistance;

        double xDistance = ellipse2.getCenterX() - ellipse1.getCenterX();
        double yDistance = ellipse2.getCenterY() - ellipse1.getCenterY();
        double distanceSquared = xDistance * xDistance + yDistance * yDistance;

        return distanceSquared <= minDistanceSquared;
    }
}
