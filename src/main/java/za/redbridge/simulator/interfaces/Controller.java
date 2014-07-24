package za.redbridge.simulator.interfaces;

import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 * Interface to the agent controller.
 * THIS WILL CHANGE MOST PROBABLY MAYBE
 * Created by jamie on 2014/07/23.
 */
public interface Controller {

    /**
     * Process the current state and provide output
     * @param environment the current environment state
     * @return the vector for driving the actuators
     */
    Double2D process(Continuous2D environment);

    /**
     * Controller that outputs a one vector. For testing.
     */
    public static final Controller DUMMY_CONTROLLER = new Controller() {
        private final Double2D one = new Double2D(1.0, 1.0);

        @Override
        public Double2D process(Continuous2D environment) {
            return one;
        }
    };
}
