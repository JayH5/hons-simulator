package za.redbridge.simulator;

import org.jbox2d.collision.AABB;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;
import za.redbridge.simulator.object.PhysicalObject;


import static za.redbridge.simulator.Utils.createAABB;
import static za.redbridge.simulator.Utils.equal;
import static za.redbridge.simulator.Utils.moveAABB;
import static za.redbridge.simulator.Utils.randomRange;
import static za.redbridge.simulator.Utils.resizeAABB;
import static za.redbridge.simulator.Utils.toDouble2D;

/**
 * Describes an area for placing objects. Typically, an object factory will request some space in
 * the area and the PlacementArea instance will return a {@link Space} object if that space is
 * available. The factory must then confirm the usage of that space by registering the object to be
 * placed with {@link #placeObject(Space, PhysicalObject)}.
 * Created by jamie on 2014/08/21.
 */
public class PlacementArea {

    private final double width;
    private final double height;

    private final MersenneTwisterFast random = new MersenneTwisterFast();

    // Need an ordered map, hence the use of a linked hashmap
    private final Map<PhysicalObject, Space> placements = new LinkedHashMap<>();

    PlacementArea(double width, double height) {
        this.width = width;
        this.height = height;
    }

    double getWidth() {
        return width;
    }

    double getHeight() {
        return height;
    }

    void setSeed(long seed) {
        random.setSeed(seed);
    }

    Space getRandomSpace(double width, double height) {
        final int maxTries = 1000;
        int tries = 1;

        float minX = (float) (0 + width / 2);
        float maxX = (float) (this.width - width / 2);
        float minY = (float) (0 + height / 2);
        float maxY = (float) (this.height - height / 2);

        AABB aabb = new AABB();
        resizeAABB(aabb, (float) width, (float) height);
        do {
            if (tries++ >= maxTries) {
                throw new RuntimeException("Unable to find space for object");
            }

            float x = randomRange(random, minX, maxX);
            float y = randomRange(random, minY, maxY);
            moveAABB(aabb, x, y);

        } while (overlappingWithOtherObject(aabb));

        return new Space(aabb);
    }

    Space getRandomSpace(double radius) {
        double diameter = radius * 2;
        return getRandomSpace(diameter, diameter);
    }

    Space getSpaceAtPosition(double width, double height, Double2D position) {
        AABB aabb =
                createAABB((float) position.x, (float) position.y, (float) width, (float) height);
        if (overlappingWithOtherObject(aabb)) {
            return null;
        }

        return new Space(aabb);
    }

    Space getSpaceAtPosition(double radius, Double2D position) {
        double diameter = radius * 2;
        return getSpaceAtPosition(diameter, diameter, position);
    }

    boolean overlappingWithOtherObject(double width, double height, Double2D position) {
        AABB aabb =
                createAABB((float) position.x, (float) position.y, (float) width, (float) height);
        return overlappingWithOtherObject(aabb);
    }

    boolean overlappingWithOtherObject(AABB aabb) {
        for (Space space : placements.values()) {
            if (AABB.testOverlap(aabb, space.aabb)) {
                return true;
            }
        }
        return false;
    }

    void placeObject(Space space, PhysicalObject object) {
        if (space.isUsed()) {
            throw new IllegalArgumentException("Space already used");
        }

        if (overlappingWithOtherObject(space.aabb)) {
            throw new IllegalArgumentException("Placement space is not available");
        }

        if (!equal(space.getPosition(), object.getBody().getPosition())) {
            throw new IllegalArgumentException(
                    "Object body position does not match placement space");
        }

        placements.put(object, space);
        space.markUsed();
    }

    Set<PhysicalObject> getPlacedObjects() {
        return placements.keySet();
    }

    /**
     * Describes some space available in the PlacementArea
     */
    public static class Space {
        private final AABB aabb;

        private boolean used = false;

        private Space(AABB aabb) {
            this.aabb = aabb;
        }

        /** Get the center position of the space */
        public Double2D getPosition() {
            return toDouble2D(aabb.getCenter());
        }

        /** Get the width of the space */
        public float getWidth() {
            return aabb.upperBound.x - aabb.lowerBound.x;
        }

        /** Get the height of the space */
        public float getHeight() {
            return aabb.upperBound.y - aabb.lowerBound.x;
        }

        private boolean isUsed() {
            return used;
        }

        private void markUsed() {
            used = true;
        }

    }

    /**
     * A wrapper for a typed placement area. Simply delegates calls to the underlying
     * PlacementArea while preserving type safety of objects to be added.
     * @param <T> The type that can be placed in this placement area
     */
    public class ForType<T extends PhysicalObject> {
        public ForType() {
        }

        /** Get the width of the placement area */
        public double getWidth() {
            return PlacementArea.this.getWidth();
        }

        /** Get the height of the placement area */
        public double getHeight() {
            return PlacementArea.this.getHeight();
        }

        /**
         * Try and get a placement space for an object of the given size at the given position.
         * Returns null if insufficient space at position.
         * @param width width of the object to be placed
         * @param height height of the object to be placed
         * @param position desired position to be placed
         * @return Space if available
         */
        public Space getSpaceAtPosition(double width, double height, Double2D position) {
            return PlacementArea.this.getSpaceAtPosition(width, height, position);
        }

        /**
         * Try and get a placement space for an object of the given size at the given position.
         * Returns null if insufficient space at position.
         * @param radius radius of the object to be placed
         * @param position desired position to be placed
         * @return Space if available
         */
        public Space getSpaceAtPosition(double radius, Double2D position) {
            return PlacementArea.this.getSpaceAtPosition(radius, position);
        }

        /**
         * Get a random placement space of the given size
         * @param width width of the object to be placed
         * @param height height of the object to be placed
         * @return a free random space
         */
        public Space getRandomSpace(double width, double height) {
            return PlacementArea.this.getRandomSpace(width, height);
        }

        /**
         * Get a random placement space of the given size
         * @param radius radius of the object to be placed
         * @return a free random space
         */
        public Space getRandomSpace(double radius) {
            return PlacementArea.this.getRandomSpace(radius);
        }

        /**
         * Check if an object of the given size at the given position would overlap with another
         * object that has already been placed.
         * @param width width of the object to be placed
         * @param height height of the object to be placed
         * @param position position to check
         * @return true if the object would overlap
         */
        public boolean overlappingWithOtherObject(double width, double height, Double2D position) {
            return PlacementArea.this.overlappingWithOtherObject(width, height, position);
        }

        /**
         * Register the object a placed in the given space.
         * Will throw exception if space already taken or object position does not match space
         * position.
         * @param space the space to place the object
         * @param object the object to be placed
         */
        public void placeObject(Space space, T object) {
            PlacementArea.this.placeObject(space, object);
        }
    }

}
