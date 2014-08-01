package za.redbridge.simulator;

import java.awt.Color;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.physics2D.PhysicsEngine2D;
import sim.util.Double2D;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.WallObject;


import static za.redbridge.simulator.Utils.randomRange;

/**
 * The main simulation state.
 *
 * Created by jamie on 2014/07/24.
 */
public class Simulation extends SimState {

    private static final int WIDTH = 102;
    private static final int HEIGHT = 102;
    private static final int WALL_THICKNESS = 1;

    private final Continuous2D environment = new Continuous2D(1.0, WIDTH, HEIGHT);
    //private final PhysicsEngine physicsEngine = new PhysicsEngine();
    private final PhysicsEngine2D physicsEngine = new PhysicsEngine2D();

    // number of robot agents
    private static final int NUM_ROBOTS = 20;
    private static final double ROBOT_RADIUS = 2.0;
    private static final double ROBOT_MASS = 1.0;

    //number of large objects
    private static final int NUM_LARGE_OBJECTS = 5;
    private static final double LARGE_OBJECT_WIDTH = 3.0;
    private static final double LARGE_OBJECT_HEIGHT = 3.0;
    private static final double LARGE_OBJECT_VALUE = 100.0;
    private static final double LARGE_OBJECT_MASS = 10.0;

    //number of small objects
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

        createWalls();
        createAgents();
        createResources();

        schedule.scheduleRepeating(physicsEngine);
    }

    private void createWalls() {
        // Left
        Double2D pos = new Double2D(WALL_THICKNESS / 2.0, HEIGHT / 2.0);
        WallObject wall = new WallObject(pos, WALL_THICKNESS, HEIGHT);
        environment.setObjectLocation(wall, pos);
        physicsEngine.register(wall);

        // Right
        pos = new Double2D(WIDTH - WALL_THICKNESS / 2.0, HEIGHT / 2.0);
        wall = new WallObject(pos, WALL_THICKNESS, HEIGHT);
        environment.setObjectLocation(wall, pos);
        physicsEngine.register(wall);

        // Top
        pos = new Double2D(WIDTH / 2.0, WALL_THICKNESS / 2.0);
        wall = new WallObject(pos, WIDTH - WALL_THICKNESS * 2, WALL_THICKNESS);
        environment.setObjectLocation(wall, pos);
        physicsEngine.register(wall);

        // Bottom
        pos = new Double2D(WIDTH / 2.0, HEIGHT - WALL_THICKNESS / 2.0);
        wall = new WallObject(pos, WIDTH - WALL_THICKNESS * 2, WALL_THICKNESS);
        environment.setObjectLocation(wall, pos);
        physicsEngine.register(wall);
    }

    // Find a random position within the environment that is away from other objects
    private Double2D findPositionForObject(double width, double height) {
        double minX = WALL_THICKNESS + width;
        double minY = WALL_THICKNESS + height;
        double maxX = WIDTH - WALL_THICKNESS - width;
        double maxY = HEIGHT - WALL_THICKNESS - height;

        final int maxTries = 1000;
        int tries = 1;

        Double2D pos;
        do {
            if (tries++ >= maxTries) {
                throw new RuntimeException("Unable to find space for object");
            }
            pos = new Double2D(randomRange(random, minX, maxX), randomRange(random, minY, maxY));
        } while (!environment.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());
        return pos;
    }


    private void createAgents() {
        Color color = new Color(106, 128, 200);

        for (int i = 0; i < NUM_ROBOTS; i++) {
            Double2D pos = findPositionForObject(ROBOT_RADIUS, ROBOT_RADIUS);
            RobotObject robot = new RobotObject(pos, ROBOT_MASS, ROBOT_RADIUS, color);
            environment.setObjectLocation(robot, pos);
            physicsEngine.register(robot);

            Double2D velocity = new Double2D(random.nextDouble() * 1.5, random.nextDouble() * 1.5);
            robot.setVelocity(velocity);
            schedule.scheduleRepeating(robot);
        }
    }

    private void createResources() {
        Color color = new Color(255, 235, 82);

        for (int i = 0; i < NUM_SMALL_OBJECTS; i++) {
            Double2D pos = findPositionForObject(SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT);
            ResourceObject resource = new ResourceObject(pos, SMALL_OBJECT_MASS, SMALL_OBJECT_WIDTH,
                    SMALL_OBJECT_HEIGHT, color, SMALL_OBJECT_VALUE);
            environment.setObjectLocation(resource, pos);
            physicsEngine.register(resource);
            schedule.scheduleRepeating(resource);
        }

        for (int i = 0; i < NUM_LARGE_OBJECTS; i++) {
            Double2D pos = findPositionForObject(LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT);
            ResourceObject resource = new ResourceObject(pos, LARGE_OBJECT_MASS, LARGE_OBJECT_WIDTH,
                    LARGE_OBJECT_HEIGHT, color, LARGE_OBJECT_VALUE);
            environment.setObjectLocation(resource, pos);
            physicsEngine.register(resource);
            schedule.scheduleRepeating(resource);
        }
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

    public PhysicsEngine2D getPhysicsEngine() {
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
