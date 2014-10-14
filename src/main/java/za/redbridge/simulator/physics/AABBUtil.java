package za.redbridge.simulator.physics;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;

/**
 * A set of utility methods for axis-aligned bounding boxes.
 * 
 * Created by jamie on 2014/10/14.
 */
public final class AABBUtil {

    private AABBUtil() {
    }

    /** Tests if the provided point is inside the provided AABB. */
    public static boolean testPoint(Vec2 point, AABB aabb) {
        return point.x >= aabb.lowerBound.x && point.x <= aabb.upperBound.x
                && point.y >= aabb.lowerBound.y && point.y <= aabb.upperBound.y;
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

    /** Gets the width of the given AABB */
    public static float getAABBWidth(AABB aabb) {
        return aabb.upperBound.x - aabb.lowerBound.x;
    }

    /** Gets the height of the given AABB */
    public static float getAABBHeight(AABB aabb) {
        return aabb.upperBound.y - aabb.lowerBound.y;
    }
}
