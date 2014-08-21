package za.redbridge.simulator;

import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.DefaultFitnessFunction;
import za.redbridge.simulator.interfaces.ResourceFactory;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.sensor.SensorContactListener;


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
    private PlacementArea placementArea;

    private final SensorContactListener contactListener = new SensorContactListener();

    private static final float TIME_STEP = 16.0f; // 16ms = 60fps
    private static final int VELOCITY_ITERATIONS = 2;
    private static final int POSITION_ITERATIONS = 1;

    private TargetAreaObject targetArea;

    private final RobotFactory robotFactory;
    private final ResourceFactory resourceFactory;
    private final SimConfig config;

    public Simulation(RobotFactory robotFactory, ResourceFactory resourceFactory,
            SimConfig config) {
        super(config.getSeed());
        this.robotFactory = robotFactory;
        this.resourceFactory = resourceFactory;
        this.config = config;
        Settings.velocityThreshold = VELOCITY_THRESHOLD;
    }

    @Override
    public void start() {
        super.start();

        environment = new Continuous2D(1.0, config.getEnvSize().x, config.getEnvSize().y);
        physicsWorld = new World(new Vec2(0f, 0f));
        placementArea = new PlacementArea(config.getEnvSize().x, config.getEnvSize().y);
        placementArea.setSeed(config.getSeed());
        schedule.reset();
        System.gc();

        physicsWorld.setContactListener(contactListener);

        // Create ALL the objects
        createWalls();
        createTargetArea();
        robotFactory
                .placeInstances(placementArea.new ForType<>(), physicsWorld, config.getNumRobots());
        resourceFactory.placeInstances(placementArea.new ForType<>(), physicsWorld,
                config.getNumResources());


        // Now actually add the objects that have been placed to the world and schedule
        for (PhysicalObject object : placementArea.getPlacedObjects()) {
            environment.setObjectLocation(object.getPortrayal(),
                    toDouble2D(object.getBody().getPosition()));
            schedule.scheduleRepeating(object);
        }

        schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                physicsWorld.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            }
        });
    }

    //end behaviour
    @Override
    public void finish() {
        kill();
        System.out.println("Total Fitness: " + getFitness());
    }

    // Walls are simply added to environment since they do not need updating
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

        // Add target area to placement area (trust that space returned since nothing else placed
        // yet).
        PlacementArea.Space space = placementArea.getSpaceAtPosition(width, height, pos);
        placementArea.placeObject(space, targetArea);
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
