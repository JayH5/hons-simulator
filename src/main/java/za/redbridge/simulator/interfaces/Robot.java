package za.redbridge.simulator.interfaces;

import sim.util.Double2D;

/**
 * Interface to the robot actuators.
 * THIS WILL CHANGE MOST PROBABLY MAYBE
 * Created by jamie on 2014/07/23.
 */
public interface Robot {
    /**
     *
     * @param controllerOutput the input to the actuators from the controller
     * @return an acceleration vector
     */
    Double2D move(Double2D controllerOutput);

    /**
     * Dummy implementation that outputs a zero vector. Just for testing.
     */
    public static final Robot DUMMY_ROBOT = new Robot() {
        private final Double2D zero = new Double2D(.0, .0);

        @Override
        public Double2D move(Double2D controllerOutput) {
            return zero;
        }
    };
}
