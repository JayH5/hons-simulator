package za.redbridge.simulator;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;
import java.util.List;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.DefaultFitnessFunction;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.sensor.SensorContactListener;


import static za.redbridge.simulator.Utils.moveAABB;
import static za.redbridge.simulator.Utils.randomRange;
import static za.redbridge.simulator.Utils.resizeAABB;
import static za.redbridge.simulator.Utils.toDouble2D;

/**
 * The main simulation state.
 *
 * Created by jamie on 2014/07/24.
 */
public class Simulation extends SimState {

    private static final float VELOCITY_THRESHOLD = 0.000001f;

    private Continuous2D environment;
    private World physicsWorld;

    private final SensorContactListener contactListener = new SensorContactListener();
    private final List<PhysicalObject> objects = new ArrayList<>();

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

    //derive the value of this resource from its area
    private boolean getValueFromArea = true;
    private TargetAreaObject targetArea;

    private RobotFactory rf;
    private final SimConfig config;

    public Simulation(RobotFactory rf, SimConfig config) {
        super(config.getSeed());
        this.rf = rf;
        this.config = config;
        Settings.velocityThreshold = VELOCITY_THRESHOLD;
        this.getValueFromArea = config.getValueFromArea();
    }

    @Override
    public void start() {
        super.start();

        environment = new Continuous2D(1.0, config.getEnvSize().x, config.getEnvSize().y);
        physicsWorld = new World(new Vec2(0f, 0f));
        schedule.reset();
        objects.clear();
        System.gc();

        physicsWorld.setContactListener(contactListener);

        createWalls();
        createTargetArea();

        List<RobotObject> robots = rf.createInstances(physicsWorld, config.getNumRobots());
        for(RobotObject robot : robots){
            addObject(robot, toDouble2D(robot.getBody().getPosition()));
        }

        createResources();

        schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                physicsWorld.step(TIME_STEP, VELOCITY_ITERATIONS,
                        POSITION_ITERATIONS);
            }
        });
    }

    //end behaviour
    @Override
    public void finish() {
        kill();
        System.out.println("Total Fitness: " + getFitness());
    }

    /** Add an object to the world in the given position. */
    private void addObject(PhysicalObject object, Double2D position) {
        environment.setObjectLocation(object.getPortrayal(), position);
        objects.add(object);
        schedule.scheduleRepeating(object);
    }

    private void createWalls() {
        // Left
        Double2D pos = new Double2D(-1, config.getEnvSize().y / 2.0);
        WallObject wall = new WallObject(physicsWorld, pos, 1, config.getEnvSize().y + 2);
        addObject(wall, pos);

        // Right
        pos = new Double2D(config.getEnvSize().x + 1, config.getEnvSize().y / 2.0);
        wall = new WallObject(physicsWorld, pos, 1, config.getEnvSize().y + 2);
        addObject(wall, pos);

        // Top
        pos = new Double2D(config.getEnvSize().x / 2.0, -1);
        wall = new WallObject(physicsWorld, pos, config.getEnvSize().x + 2, 1);
        addObject(wall, pos);

        // Bottom
        pos = new Double2D(config.getEnvSize().x / 2.0, config.getEnvSize().y + 1);
        wall = new WallObject(physicsWorld, pos, config.getEnvSize().x + 2, 1);
        addObject(wall, pos);
    }

    //create target area
    private void createTargetArea() {
        final int width, height;
        final Double2D pos;

        if (config.getTargetAreaPlacement() == SimConfig.Direction.NORTH) {
            width = config.getEnvSize().x;
            height = config.getTargetAreaThickness();
            pos = new Double2D(width/2,height/2);
        } else if (config.getTargetAreaPlacement() == SimConfig.Direction.SOUTH) {
            width = config.getEnvSize().x;
            height = config.getTargetAreaThickness();
            pos = new Double2D(config.getEnvSize().x - width/2, config.getEnvSize().y - height/2);
        } else if (config.getTargetAreaPlacement() == SimConfig.Direction.EAST) {
            width = config.getTargetAreaThickness();
            height = config.getEnvSize().y;
            pos = new Double2D(config.getEnvSize().x - width/2, height/2);
        } else if (config.getTargetAreaPlacement() == SimConfig.Direction.WEST) {
            width = config.getTargetAreaThickness();
            height = config.getEnvSize().y;
            pos = new Double2D(width/2,height/2);
        } else {
            return; // Don't know where to place this target area
        }

        //for now just give it the default fitness function
        targetArea = new TargetAreaObject(physicsWorld, pos, width, height,
                                                            new DefaultFitnessFunction());

        addObject(targetArea, pos);
    }

    // Find a random position within the environment that is away from other objects
    private Double2D findPositionForObject(double width, double height) {
        final int maxTries = 1000;
        int tries = 1;

        double minX = 0 + width / 2;
        double maxX = environment.getWidth() - width / 2;
        double minY = 0 + height / 2;
        double maxY = environment.getHeight() - height / 2;

        AABB aabb = new AABB();
        resizeAABB(aabb, (float) width, (float) height);
        Double2D pos;
        do {
            if (tries++ >= maxTries) {
                throw new RuntimeException("Unable to find space for object");
            }

            pos = new Double2D(randomRange(random, minX, maxX), randomRange(random, minY, maxY));
            moveAABB(aabb, (float) pos.x, (float) pos.y);

        } while (overlappingWithOtherObject(aabb));

        return pos;
    }

    /** Returns true if the given AABB intersects with any other object's. */
    private boolean overlappingWithOtherObject(AABB aabb) {
        for (PhysicalObject object : objects) {
            AABB otherAABB = object.getBody().getFixtureList().getAABB(0);
            if (AABB.testOverlap(aabb, otherAABB)) {
                return true;
            }
        }
        return false;
    }

    private void createResources() {
        double value = getValueFromArea ?
                SMALL_OBJECT_WIDTH * SMALL_OBJECT_HEIGHT : SMALL_OBJECT_VALUE;
        for (int i = 0; i < NUM_SMALL_OBJECTS; i++) {
            Double2D pos = findPositionForObject(SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT);
            ResourceObject resource = new ResourceObject(physicsWorld, pos, SMALL_OBJECT_WIDTH,
                    SMALL_OBJECT_HEIGHT, SMALL_OBJECT_MASS, value);

            addObject(resource, pos);
        }

        value = getValueFromArea ?
                LARGE_OBJECT_WIDTH * LARGE_OBJECT_HEIGHT : LARGE_OBJECT_VALUE;
        for (int i = 0; i < NUM_LARGE_OBJECTS; i++) {
            Double2D pos = findPositionForObject(LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT);
            ResourceObject resource = new ResourceObject(physicsWorld, pos, LARGE_OBJECT_WIDTH,
                    LARGE_OBJECT_HEIGHT, LARGE_OBJECT_MASS, value);

            addObject(resource, pos);
        }
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);
        config.setSeed(seed);
    }

    /** Get the environment (forage area) for this simulation. */
    public Continuous2D getEnvironment() {
        return environment;
    }

    public void runForNIterations(int n) {
        for (int i = 0; i < n; i++) {
            schedule.step(this);
        }
    }

    //return the score at this point in the simulation
    public double getFitness() { return targetArea.getTotalFitness(); }

    /**
     * Launching the application from this main method will run the simulation in headless mode.
     */
    public static void main (String[] args) {
        doLoop(Simulation.class, args);
        System.exit(0);
    }
}
