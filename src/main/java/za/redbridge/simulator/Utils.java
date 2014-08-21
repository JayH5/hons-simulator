package za.redbridge.simulator;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;

/**
 * Created by jamie on 2014/08/01.
 */
public final class Utils {

    private static final double EPSILON = 0.0001;

    public static final double HALF_PI = Math.PI / 2;
    public static final double TWO_PI = Math.PI * 2;

    private Utils() {
    }

    public static double randomRange(MersenneTwisterFast rand, double from, double to) {
        if (from >= to) {
            throw new IllegalArgumentException("`from` must be less than `to`");
        }

        double range = to - from;
        return rand.nextDouble() * range + from;
    }

    public static float randomRange(MersenneTwisterFast rand, float from, float to) {
        if (from >= to) {
            throw new IllegalArgumentException("`from` must be less than `to`");
        }

        float range = to - from;
        return rand.nextFloat() * range + from;
    }

    /** Check if two doubles are roughly equal */
    public static boolean equal(double lhs, double rhs) {
        return Math.abs(lhs - rhs) <= EPSILON;
    }

    /** Check if two Double2Ds are roughly equal */
    public static boolean equal(Double2D lhs, Double2D rhs) {
        return equal(lhs.x, rhs.x) && equal(lhs.y, rhs.y);
    }

    /** Check if a Double2D is roughly equal to a Vec2 */
    public static boolean equal(Double2D lhs, Vec2 rhs) {
        return equal(lhs.x, rhs.x) && equal(lhs.y, rhs.y);
    }

    /**
     * Normalises an angle to the range [-PI, PI]. Probably not a good idea to put very large
     * numbers into this method.
     * @param radians input angle
     * @return normalised angle
     */
    public static double normaliseAngle(double radians) {
        while (radians < -Math.PI) {
            radians += TWO_PI;
        }
        while (radians > Math.PI) {
            radians -= TWO_PI;
        }

        return radians;
    }

    /**
     * Returns the angle between two points.
     * @param p1 the first point
     * @param p2 the second point
     * @return angle between the points [-PI, PI]
     */
    public static double angleBetweenPoints(Double2D p1, Double2D p2) {
        return Math.atan2(p2.y - p1.y, p2.x - p1.x);
    }

    public static Vec2 toVec2(Double2D double2D) {
        return new Vec2((float) double2D.x, (float) double2D.y);
    }

    public static Double2D toDouble2D(Vec2 vec2) {
        return new Double2D(vec2.x, vec2.y);
    }

    /** Move an AABB, keeping the same size. */
    public static void moveAABB(AABB aabb, float x, float y) {
        // Upper bound is top right vertex
        // Lower bound is bottom left vertex
        float halfWidth = (aabb.upperBound.x - aabb.lowerBound.x) / 2;
        float halfHeight = (aabb.upperBound.y - aabb.lowerBound.y) / 2;

        aabb.upperBound.set(x + halfWidth, y + halfHeight);
        aabb.lowerBound.set(x - halfWidth, y - halfHeight);
    }

    /** Resize an AABB, keeping the same center position. */
    public static void resizeAABB(AABB aabb, float width, float height) {
        Vec2 center = aabb.getCenter();

        float halfWidth = width / 2;
        float halfHeight = height / 2;

        aabb.upperBound.set(center.x + halfWidth, center.y + halfHeight);
        aabb.lowerBound.set(center.x - halfWidth, center.y - halfHeight);
    }

    public static AABB createAABB(float x, float y, float width, float height) {
        float halfWidth = width / 2;
        float halfHeight = height / 2;

        Vec2 lowerBound = new Vec2(x - halfWidth, y - halfHeight);
        Vec2 upperBound = new Vec2(x + halfWidth, y + halfHeight);

        return new AABB(lowerBound, upperBound);
    }
}
