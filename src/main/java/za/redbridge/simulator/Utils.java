package za.redbridge.simulator;

import ec.util.MersenneTwisterFast;

/**
 * Created by jamie on 2014/08/01.
 */
public final class Utils {
    private Utils() {
    }

    public static double randomRange(MersenneTwisterFast rand, double from, double to) {
        if (from >= to) {
            throw new IllegalArgumentException("`from` must be less than `to`");
        }

        double range = to - from;
        return rand.nextDouble() * range + from;
    }
}
