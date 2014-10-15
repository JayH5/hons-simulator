package za.redbridge.simulator;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

import java.util.Random;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;

/**
 * Created by jamie on 2014/08/01.
 */
public final class Utils {

    public static final double TWO_PI = Math.PI * 2;
    public static final double EPSILON = 1e-6;

    public static final Random RANDOM = new Random();

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

    public static float randomUniformRange(float from, float to) {

        Random rand = new Random();

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

    public static boolean isNearlyZero(double x) {
        return x > -EPSILON && x < EPSILON;
    }

    public static Vec2 jitter(Vec2 vec, float magnitude) {
        if (vec != null) {
            vec.x += magnitude * RANDOM.nextFloat() - magnitude / 2;
            vec.y += magnitude * RANDOM.nextFloat() - magnitude / 2;
            return vec;
        }
        return null;
    }

    /** Get a random angle in the range [-PI / 2, PI / 2] */
    public static float randomAngle(MersenneTwisterFast random) {
        return MathUtils.TWOPI * random.nextFloat() - MathUtils.PI;
    }

}
