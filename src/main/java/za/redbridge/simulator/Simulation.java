package za.redbridge.simulator;

import org.jbox2d.common.Settings;
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
import za.redbridge.simulator.ea.DefaultFitnessFunction;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.sensor.SensorContactListener;


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

    //derive the value of this resource from its area
    private boolean getValueFromArea = true;
    private TargetAreaObject targetArea;

    private RobotFactory rf;
    private final SimConfig config;

    public Simulation(RobotFactory rf, SimConfig config) {
        super(config.getSeed());
        this.rf = rf;
        this.config = config;
        this.environment = new Continuous2D(1.0, config.getEnvSize().x, config.getEnvSize().y);
        this.physicsWorld = new World(new Vec2(0f, 0f));
        Settings.velocityThreshold = VELOCITY_THRESHOLD;
        this.getValueFromArea = config.getValueFromArea();
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
        createTargetArea();
        createResources();

        physicsWorld.setContactListener(new SensorContactListener());

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

    //create target area
    private void createTargetArea() {

        int width = 0, height = 0;
        Double2D pos = new Double2D ();

        if (config.getTargetAreaPlacement() == SimConfig.Direction.NORTH) {
            width = config.getEnvSize().x;
            height = config.getTargetAreaThickness();
            pos = new Double2D(width/2,height/2);
        }
        else if (config.getTargetAreaPlacement() == SimConfig.Direction.SOUTH) {
            width = config.getEnvSize().x;
            height = config.getTargetAreaThickness();
            pos = new Double2D(config.getEnvSize().x - width/2, config.getEnvSize().y - height/2);
        }
        else if (config.getTargetAreaPlacement() == SimConfig.Direction.EAST) {
            width = config.getTargetAreaThickness();
            height = config.getEnvSize().y;
            pos = new Double2D(config.getEnvSize().x - width/2, height/2);
        }
        else if (config.getTargetAreaPlacement() == SimConfig.Direction.WEST) {
            width = config.getTargetAreaThickness();
            height = config.getEnvSize().y;
            pos = new Double2D(width/2,height/2);
        }

        //for now just give it the default fitness function
        targetArea = new TargetAreaObject(physicsWorld, pos, width, height,
                                                            new DefaultFitnessFunction());

        environment.setObjectLocation(targetArea.getPortrayal(), pos);
    }

    //sorry im mad at bath, these caluclate min/max coord values of the effective forage area. prob better way lay this out
    //and calculate things
    private Double2D calculateForageAreaXRange() {

        double min = 0, max = 0;

        if (config.getTargetAreaPlacement() == SimConfig.Direction.NORTH ||
                config.getTargetAreaPlacement() == SimConfig.Direction.SOUTH) {
            min = 0;
            max = config.getEnvSize().x;
        }
        else if (config.getTargetAreaPlacement() == SimConfig.Direction.EAST) {
            min = 0;
            max = config.getEnvSize().x-targetArea.getWidth();
        }
        else if (config.getTargetAreaPlacement() == SimConfig.Direction.WEST) {
            min = targetArea.getWidth();
            max = config.getEnvSize().x;
        }

        return new Double2D(min,max);
    }

    private Double2D calculateForageAreaYRange() {

        double min = 0, max = 0;

        if (config.getTargetAreaPlacement() == SimConfig.Direction.NORTH) {
            min = targetArea.getHeight();
            max = config.getEnvSize().y;
        }
        else if (config.getTargetAreaPlacement() == SimConfig.Direction.SOUTH) {
            min = 0;
            max = config.getEnvSize().y - targetArea.getHeight();
        }
        else if (config.getTargetAreaPlacement() == SimConfig.Direction.EAST ||
                config.getTargetAreaPlacement() == SimConfig.Direction.WEST) {
            min = 0;
            max = config.getEnvSize().y;
        }

        return new Double2D(min,max);
    }

    // Find a random position within the environment that is away from other objects and forage area
    private Double2D findPositionForObject(double width, double height) {
        final int maxTries = 1000;
        int tries = 1;

        Double2D pos;
        do {
            if (tries++ >= maxTries) {
                throw new RuntimeException("Unable to find space for object");
            }

            double minX, maxX, minY, maxY;
            minX = calculateForageAreaXRange().x;
            maxX = calculateForageAreaXRange().y;

            minY = calculateForageAreaYRange().x;
            maxY = calculateForageAreaYRange().y;

            pos = new Double2D(randomRange(random, minX, maxX),
                    randomRange(random, minY, maxY));

        } while (!environment.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());
        return pos;
    }

    private void createResources() {
        for (int i = 0; i < NUM_SMALL_OBJECTS; i++) {
            Double2D pos = findPositionForObject(SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT);
            ResourceObject resource;

            if (getValueFromArea) {
                resource = new ResourceObject(physicsWorld, pos, SMALL_OBJECT_WIDTH,
                        SMALL_OBJECT_HEIGHT, SMALL_OBJECT_MASS, RESOURCE_COLOUR, SMALL_OBJECT_WIDTH*SMALL_OBJECT_HEIGHT);
            }
            else {
                resource = new ResourceObject(physicsWorld, pos, SMALL_OBJECT_WIDTH,
                        SMALL_OBJECT_HEIGHT, SMALL_OBJECT_MASS, RESOURCE_COLOUR, SMALL_OBJECT_VALUE);
            }

            environment.setObjectLocation(resource.getPortrayal(), pos);
            schedule.scheduleRepeating(resource);
        }

        for (int i = 0; i < NUM_LARGE_OBJECTS; i++) {
            Double2D pos = findPositionForObject(LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT);
            ResourceObject resource;

            if (getValueFromArea) {
                resource = new ResourceObject(physicsWorld, pos, LARGE_OBJECT_WIDTH,
                        LARGE_OBJECT_HEIGHT, LARGE_OBJECT_MASS, RESOURCE_COLOUR, LARGE_OBJECT_WIDTH*LARGE_OBJECT_HEIGHT);
            }
            else {
                resource = new ResourceObject(physicsWorld, pos, LARGE_OBJECT_WIDTH,
                        LARGE_OBJECT_HEIGHT, LARGE_OBJECT_MASS, RESOURCE_COLOUR, LARGE_OBJECT_VALUE);
            }

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

    //get overall fitness
    public double getFitness() { return targetArea.getTotalFitness(); }

    public void runForNIterations(int n) {
        for (int i = 0; i < n; i++) {
            schedule.step(this);
        }
    }

    //return the score at this point in the simulation
    public double returnScore() { return targetArea.getTotalFitness(); }

    /**
     * Launching the application from this main method will run the simulation in headless mode.
     */
    public static void main (String[] args) {
        doLoop(Simulation.class, args);
        System.exit(0);
    }
}
