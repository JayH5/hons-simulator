package za.redbridge.simulator.object;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
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
 * All AgentObjects are round with a fixed radius.
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

    private final Ellipse2D.Double collisionEllipse = new Ellipse2D.Double();
    private final double radius;

    public AgentObject(Double2D initialPosition, double radius, Controller controller,
            Robot robot) {
        position = initialPosition;
        this.radius = radius;
        portrayal = new OvalPortrayal2D(radius * 2, radius * 2);
        portrayal.setPaint(Color.BLUE);
        this.controller = controller;
        this.robot = robot;
    }

    public double getRadius() {
        return radius;
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

        // Check if we have collided with something
        if (!isColliding(environment, newPosition)) {
            // Check if agent has actually moved before updating
            if (position.x != newPosition.x || position.y != newPosition.y) {
                environment.setObjectLocation(portrayal, position);
            }

            velocity = newVelocity;
            position = newPosition;
        } else {
            velocity = new Double2D(); // ZERO
        }
    }

    private boolean isColliding(Continuous2D environment, Double2D atPosition) {
        if (isCollidingWithWall(environment, atPosition)) {
            return true;
        }

        double distance = radius + Simulation.MAX_OBJECT_RADIUS;
        Bag neighbours = environment.getNeighborsWithinDistance(atPosition, distance);
        if (neighbours != null && !neighbours.isEmpty()) {
            for (Object obj : neighbours) {
                if (obj instanceof ResourceObject &&
                        isCollidingWith((ResourceObject) obj, atPosition)) {
                    return true;
                } else if (obj instanceof AgentObject &&
                        isCollidingWith((AgentObject) obj, atPosition)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isCollidingWithWall(Continuous2D environment, Double2D atPosition) {
        double width = environment.getWidth();
        double height = environment.getHeight();

        return atPosition.x - radius <= 0.0 || atPosition.x + radius >= width
                || atPosition.y - radius <= 0.0 || atPosition.y + radius >= height;
    }

    private boolean isCollidingWith(ResourceObject object, Double2D atPosition) {
        final Ellipse2D.Double boundingEllipse = getBoundingEllipseAtPosition(atPosition);
        final Rectangle2D.Double boundingRectangle = object.getBoundingRectangle();

        // First check the bounds of the ellipse against the rectangle
        if (boundingEllipse.getBounds2D().intersects(boundingRectangle)) {
            // Now do more accurate intersects
            if (boundingEllipse.intersects(boundingRectangle)) {
                return true;
            }
        }

        return false;
    }

    // Collision detection with another agent is easy - just check distance between agents.
    private boolean isCollidingWith(AgentObject object, Double2D myPosition) {
        final Double2D theirPosition = object.getPosition();

        double distX = theirPosition.x - myPosition.x;
        double distY = theirPosition.y - myPosition.y;
        double distSquared = distX * distX + distY * distY;

        double minDist = radius + object.radius;
        return distSquared <= minDist * minDist;
    }

    public Ellipse2D.Double getBoundingEllipse() {
        double x = position.x - radius;
        double y = position.y - radius;

        collisionEllipse.setFrame(x, y, portrayal.getWidth(), portrayal.getHeight());

        return collisionEllipse;
    }

    private Ellipse2D.Double getBoundingEllipseAtPosition(Double2D position) {
        double x = position.x - radius;
        double y = position.y - radius;

        collisionEllipse.setFrame(x, y, portrayal.getWidth(), portrayal.getHeight());

        return collisionEllipse;
    }

}
