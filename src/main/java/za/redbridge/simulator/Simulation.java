package za.redbridge.simulator;

import java.awt.Color;

import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;

/**
 * The main simulation state.
 *
 * Created by jamie on 2014/07/24.
 */
public class Simulation extends SimState {

    private final Continuous2D environment = new Continuous2D(1.0, 100, 100);
    private final PhysicsEngine physicsEngine = new PhysicsEngine();

    // number of robot agents
    private static final int NUM_ROBOTS = 20;
    private static final double ROBOT_RADIUS = 2.0;
    private static final double ROBOT_MASS = 1.0;

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

    public Simulation(long seed) {
        super(seed);
    }

    @Override
    public void start() {
        super.start();

        environment.clear();

        final MersenneTwisterFast random = this.random;

        // Create the agents
        for (int i = 0; i < NUM_ROBOTS; i++) {
            // Find a random position within the environment that is away from other objects
            Double2D pos;
            do {
                pos = new Double2D(environment.getWidth() * random.nextDouble(),
                        environment.getHeight() * random.nextDouble());
            } while (!environment.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());

            RobotObject robot = new RobotObject(ROBOT_MASS, ROBOT_RADIUS, pos);
            robot.getPortrayal().setPaint(Color.BLUE);
            environment.setObjectLocation(robot.getPortrayal(), robot.getPosition());
            physicsEngine.addObject(robot);
        }

        // Create some small objects
        for (int i = 0; i < NUM_SMALL_OBJECTS; i++) {
            Double2D pos;
            do {
                pos = new Double2D(environment.getWidth() * random.nextDouble(),
                        environment.getHeight() * random.nextDouble());
            } while (!environment.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());

            ResourceObject resource = new ResourceObject(SMALL_OBJECT_MASS, SMALL_OBJECT_WIDTH,
                    SMALL_OBJECT_HEIGHT, pos, SMALL_OBJECT_VALUE);
            resource.getPortrayal().setPaint(Color.RED);
            environment.setObjectLocation(resource.getPortrayal(), resource.getPosition());
            physicsEngine.addObject(resource);
        }

        schedule.scheduleRepeating(physicsEngine);
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

    public PhysicsEngine getPhysicsEngine() {
        return physicsEngine;
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
