package za.redbridge.simulator;

import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

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
    private static final double LARGE_OBJECT_WIDTH = 3.0;
    private static final double LARGE_OBJECT_HEIGHT = 3.0;
    private static final double LARGE_OBJECT_VALUE = 100.0;
    private static final double LARGE_OBJECT_MASS = 10.0;

    //number of small objects TODO
    private static final int NUM_SMALL_OBJECTS = 10;
    private static final double SMALL_OBJECT_WIDTH = 1.5;
    private static final double SMALL_OBJECT_HEIGHT = 1.5;
    private static final double SMALL_OBJECT_VALUE = 50.0;
    private static final double SMALL_OBJECT_MASS = 5.0;

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

        // Create the agents
        /*for (int i = 0; i < NUM_ROBOTS; i++) {
            // Find a random position within the environment that is away from other objects
            Double2D pos;
            do {
                pos = new Double2D(environment.getWidth() * random.nextDouble(),
                        environment.getHeight() * random.nextDouble());
            } while (!environment.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());

            AgentObject agent = new AgentObject(pos, AGENT_RADIUS, Controller.DUMMY_CONTROLLER,
                    Robot.DUMMY_ROBOT);


            agent.scheduleRepeating(schedule);
        }

        // Create some small objects
        for (int i = 0; i < NUM_SMALL_OBJECTS; i++) {
            Double2D pos;
            do {
                pos = new Double2D(environment.getWidth() * random.nextDouble(),
                        environment.getHeight() * random.nextDouble());
            } while (!environment.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());

            ResourceObject agent = new ResourceObject(pos, SMALL_OBJECT_MASS, SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT,
                    SMALL_OBJECT_VALUE, );

            agent.placeInEnvironment(environment);
            agent.scheduleRepeating(schedule);
        }*/
    }

    /**
     * Get the environment (forage area) for this simulation.
     */
    public Continuous2D getEnvironment() {
        return environment;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    /* Example */
    public static void runForNIterations(int n) {
        Simulation sim = new Simulation(System.currentTimeMillis());
        for (int i = 0; i < n; i++) {
            sim.schedule.step(sim);
        }
    }

    /**
     * Launching the application from this main method will run the simulation in headless mode.
     */
    public static void main (String[] args) {
        doLoop(Simulation.class, args);
        System.exit(0);
    }
}
