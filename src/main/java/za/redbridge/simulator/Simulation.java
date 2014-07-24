package za.redbridge.simulator;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import za.redbridge.simulator.interfaces.Controller;
import za.redbridge.simulator.interfaces.Robot;
import za.redbridge.simulator.object.AgentObject;

/**
 * The main simulation state.
 *
 * Created by jamie on 2014/07/24.
 */
public class Simulation extends SimState {

    private final Continuous2D environment = new Continuous2D(1.0, 100, 100);

    // number of robot agents
    private static final int NUM_ROBOTS = 20;
    private static final double AGENT_RADIUS = 2.0;

    //number of large objects TODO
    private static final int NUM_LARGE_OBJECTS = 10;

    //number of small objects TODO
    private static final int NUM_SMALL_OBJECTS = 10;

    private static final int PLACEMENT_DISTANCE = 10;

    public static final double MAX_OBJECT_RADIUS = 4.0;

    public Simulation(long seed) {
        super(seed);
    }

    @Override
    public void start() {
        super.start();

        environment.clear();

        final MersenneTwisterFast random = this.random;
        for (int i = 0; i < NUM_ROBOTS; i++) {
            // Find a random position within the environment that is
            Double2D pos;
            do {
                pos = new Double2D(environment.getWidth() * random.nextDouble(),
                        environment.getHeight() * random.nextDouble());
            } while (!environment.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());

            AgentObject agent = new AgentObject(pos, AGENT_RADIUS, Controller.DUMMY_CONTROLLER,
                    Robot.DUMMY_ROBOT);

            agent.placeInEnvironment(environment);
            agent.scheduleRepeating(schedule);
        }
    }

    /**
     * Get the environment (forage area) for this simulation.
     */
    public Continuous2D getEnvironment() {
        return environment;
    }

    /**
     * Launching the application from this main method will run the simulation in headless mode.
     */
    public static void main (String[] args) {
        doLoop(Simulation.class, args);
        System.exit(0);
    }
}
