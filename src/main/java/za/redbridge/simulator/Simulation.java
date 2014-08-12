package za.redbridge.simulator;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.awt.Color;
import java.util.List;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.WallObject;


import static za.redbridge.simulator.Utils.randomRange;
import static za.redbridge.simulator.Utils.toDouble2D;

/**
 * The main simulation state.
 *
 * Created by jamie on 2014/07/24.
 */
public class Simulation extends SimState {

    private static final float VELOCITY_THRESHOLD = 0.000001f;

    private final Continuous2D environment;

    private final World physicsWorld;

    private static final float TIME_STEP = 16.0f; // 16ms = 60fps
    private static final int VELOCITY_ITERATIONS = 2;
    private static final int POSITION_ITERATIONS = 1;

    //number of large objects
    private static final int NUM_LARGE_OBJECTS = 5;
    private static final double LARGE_OBJECT_WIDTH = 5.0;
    private static final double LARGE_OBJECT_HEIGHT = 5.0;
    private static final double LARGE_OBJECT_VALUE = 100.0;
    private static final double LARGE_OBJECT_MASS = 100.0;

    //number of small objects
    private static final int NUM_SMALL_OBJECTS = 10;
    private static final double SMALL_OBJECT_WIDTH = 3;
    private static final double SMALL_OBJECT_HEIGHT = 3;
    private static final double SMALL_OBJECT_VALUE = 50.0;
    private static final double SMALL_OBJECT_MASS = 40.0;
    private static final int PLACEMENT_DISTANCE = 10;
    private static final Color AGENT_COLOUR = new Color(106, 128, 200);
    private static final Color RESOURCE_COLOUR = new Color(255, 235, 82);


    private RobotFactory rf;
    private final SimConfig config;

    public Simulation(RobotFactory rf, SimConfig config) {
        super(config.getSeed());
        this.rf = rf;
        this.config = config;
        this.environment = new Continuous2D(1.0, config.getEnvSize().x, config.getEnvSize().y);
        this.physicsWorld = new World(new Vec2(0f, 0f));
        Settings.velocityThreshold = VELOCITY_THRESHOLD;
    }

    @Override
    public void start() {
        super.start();

        environment.clear();

        List<RobotObject> robots = rf.createInstances(physicsWorld, config.getNumRobots());
        for(RobotObject robot : robots){
            Double2D position = toDouble2D(robot.getBody().getPosition());
            environment.setObjectLocation(robot.getPortrayal(), position);
            schedule.scheduleRepeating(robot);
        }
        createWalls();
        createResources();

        schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                physicsWorld.step(TIME_STEP, VELOCITY_ITERATIONS,
                        POSITION_ITERATIONS);
            }
        });
    }

    private void createWalls() {
        // Left
        Double2D pos = new Double2D(-1, config.getEnvSize().y / 2.0);
        WallObject wall = new WallObject(physicsWorld, pos, 1, config.getEnvSize().y + 2);
        environment.setObjectLocation(wall.getPortrayal(), pos);

        // Right
        pos = new Double2D(config.getEnvSize().x + 1, config.getEnvSize().y / 2.0);
        wall = new WallObject(physicsWorld, pos, 1, config.getEnvSize().y + 2);
        environment.setObjectLocation(wall.getPortrayal(), pos);

        // Top
        pos = new Double2D(config.getEnvSize().x / 2.0, -1);
        wall = new WallObject(physicsWorld, pos, config.getEnvSize().x + 2, 1);
        environment.setObjectLocation(wall.getPortrayal(), pos);

        // Bottom
        pos = new Double2D(config.getEnvSize().x / 2.0, config.getEnvSize().y + 1);
        wall = new WallObject(physicsWorld, pos, config.getEnvSize().x + 2, 1);
        environment.setObjectLocation(wall.getPortrayal(), pos);
    }

    // Find a random position within the environment that is away from other objects
    private Double2D findPositionForObject(double width, double height) {
        final int maxTries = 1000;
        int tries = 1;

        Double2D pos;
        do {
            if (tries++ >= maxTries) {
                throw new RuntimeException("Unable to find space for object");
            }
            pos = new Double2D(randomRange(random, width, config.getEnvSize().x - width), randomRange(random, height, config.getEnvSize().y - height));
        } while (!environment.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());
        return pos;
    }

    private void createResources() {
        for (int i = 0; i < NUM_SMALL_OBJECTS; i++) {
            Double2D pos = findPositionForObject(SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT);
            ResourceObject resource = new ResourceObject(physicsWorld, pos, SMALL_OBJECT_WIDTH,
                    SMALL_OBJECT_HEIGHT, SMALL_OBJECT_MASS, RESOURCE_COLOUR, SMALL_OBJECT_VALUE);
            environment.setObjectLocation(resource.getPortrayal(), pos);
            schedule.scheduleRepeating(resource);
        }

        for (int i = 0; i < NUM_LARGE_OBJECTS; i++) {
            Double2D pos = findPositionForObject(LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT);
            ResourceObject resource = new ResourceObject(physicsWorld, pos, LARGE_OBJECT_WIDTH,
                    LARGE_OBJECT_HEIGHT, LARGE_OBJECT_MASS, RESOURCE_COLOUR, LARGE_OBJECT_VALUE);
            environment.setObjectLocation(resource.getPortrayal(), pos);
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

    public void runForNIterations(int n) {
        for (int i = 0; i < n; i++) {
            schedule.step(this);
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
