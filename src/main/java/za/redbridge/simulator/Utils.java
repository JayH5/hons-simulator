package za.redbridge.simulator;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;

/**
 * Created by jamie on 2014/08/01.
 */
public final class Utils {

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

    /** Create an AABB at the given position with the given size. */
    public static AABB createAABB(float x, float y, float width, float height) {
        float halfWidth = width / 2;
        float halfHeight = height / 2;

        Vec2 lowerBound = new Vec2(x - halfWidth, y - halfHeight);
        Vec2 upperBound = new Vec2(x + halfWidth, y + halfHeight);

        return new AABB(lowerBound, upperBound);
    }

    /** Wrap an angle between (-PI, PI] */
    public static double wrapAngle(double angle) {
        angle %= TWO_PI;
        if (angle > Math.PI) {
            angle -= TWO_PI;
        } else if (angle <= -Math.PI) {
            angle += TWO_PI;
        }
        return angle;
    }

}
