package za.redbridge.simulator.object;

import java.awt.Color;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.interfaces.Controller;
import za.redbridge.simulator.interfaces.Robot;
import za.redbridge.simulator.portrayal.OvalPortrayal2D;

/**
 * Object that represents the agents in the environment. After creating an instance, call
 * {@link #placeInEnvironment(Continuous2D)} to have the object appear in the environment and call
 * {@link #scheduleRepeating(Schedule)} to have the object be updated each step of the simulation.
 *
 * Created by jamie on 2014/07/23.
 */
public class AgentObject {

    private final OvalPortrayal2D portrayal;
    private final Controller controller;
    private final Robot robot;

    private Double2D position;
    private Double2D velocity = new Double2D(.0, .0);

    private final Steppable steppable = new Steppable() {
        @Override
        public void step(SimState state) {
            performStep(state);
        }
    };
    private Stoppable stoppable;

    public AgentObject(Double2D initialPosition, double width, double height, Controller controller,
            Robot robot) {
        position = initialPosition;
        portrayal = new OvalPortrayal2D(width, height);
        portrayal.setPaint(Color.BLUE);
        this.controller = controller;
        this.robot = robot;
    }

    /**
     * Sets the object's location in the provided environment.
     */
    public void placeInEnvironment(Continuous2D environment) {
        environment.setObjectLocation(portrayal, position);
    }

    /**
     * Schedules the object on the provided schedule to receive repeating updates.
     */
    public void scheduleRepeating(Schedule schedule) {
        if (!isScheduled()) {
            stoppable = schedule.scheduleRepeating(steppable);
        }
    }

    /** Stops the object from receiving scheduled step calls if the object has been scheduled. */
    public void stop() {
        if (isScheduled()) {
            stoppable.stop();
            stoppable = null;
        }
    }

    /**
     * Returns whether the object has been scheduled to receive step updates (and hasn't been
     * stopped).
     */
    public boolean isScheduled() {
        return stoppable != null;
    }

    /** Get the portrayal associated with this agent. Useful for setting color/fill. */
    public OvalPortrayal2D getPortrayal() {
        return portrayal;
    }

    public Double2D getPosition() {
        return position;
    }

    public Double2D getVelocity() {
        return velocity;
    }

    /**
     * Each step, get the controller to process the current state and produce a value to drive the
     * robot's actuators. This value is then fed in to the {@link Robot} instance which produces an
     * acceleration value for the agent.
     * Since {@link Schedule} doesn't seem to provide any indication of the elapsed time between two
     * steps, we use the number of steps a unit of time.
     * @param state The simulation state for this step
     */
    protected void performStep(SimState state) {
        Simulation simulation = (Simulation) state;
        Continuous2D environment = simulation.getEnvironment();

        Double2D controllerOutput = controller.process(environment);
        Double2D acceleration = robot.move(controllerOutput);

        Double2D newVelocity =
                new Double2D(velocity.x + acceleration.x, velocity.y + acceleration.y);
        Double2D newPosition = new Double2D(position.x + newVelocity.x, position.y + newVelocity.y);

        // Check if agent has actually moved before updating
        if (position.x != newPosition.x || position.y != newPosition.y) {
            environment.setObjectLocation(portrayal, position);
        }

        // TODO: collision detection
        velocity = newVelocity;
        position = newPosition;
    }

}
