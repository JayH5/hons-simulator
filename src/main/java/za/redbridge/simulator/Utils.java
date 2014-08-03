package za.redbridge.simulator;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;

/**
 * Created by jamie on 2014/08/01.
 */
public final class Utils {

    private static final double EPSILON = 0.0001;

    private Utils() {
    }

    public static double randomRange(MersenneTwisterFast rand, double from, double to) {
        if (from >= to) {
            throw new IllegalArgumentException("`from` must be less than `to`");
        }

        double range = to - from;
        return rand.nextDouble() * range + from;
    }

    /** Check if two doubles are roughly equal */
    public static boolean equal(double lhs, double rhs) {
        return Math.abs(lhs - rhs) <= EPSILON;
    }

    /** Check if two Double2Ds are roughly equal */
    public static boolean equal(Double2D lhs, Double2D rhs) {
        return equal(lhs.x, rhs.x) && equal(lhs.y, rhs.y);
    }

    /**
     * Returns the angle between two points.
     * @param p1 the first point
     * @param p2 the second point
     * @return angle between the points [0, 2 * PI]
     */
    public static double angleBetweenPoints(Double2D p1, Double2D p2) {
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        if (angle < 0.0) {
            angle += Math.PI * 2;
        }
        return angle;
    }
}
