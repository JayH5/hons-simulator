package za.redbridge.simulator;

import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.factories.ResourceFactory;
import za.redbridge.simulator.factories.RobotFactory;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.object.WallObject;
import za.redbridge.simulator.physics.SimulationContactListener;


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

    private final SimulationContactListener contactListener = new SimulationContactListener();

    private static final float TIME_STEP = 1f / 10f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 3;

    private TargetAreaObject targetArea;

    private final RobotFactory robotFactory;
    private final ResourceFactory resourceFactory;
    private final SimConfig config;

    public Simulation(RobotFactory robotFactory, ResourceFactory resourceFactory,
            SimConfig config) {
        super(config.getSimulationSeed());
        this.robotFactory = robotFactory;
        this.resourceFactory = resourceFactory;
        this.config = config;
        Settings.velocityThreshold = VELOCITY_THRESHOLD;
    }

    @Override
    public void start() {
        super.start();

        environment =
                new Continuous2D(1.0, config.getEnvironmentWidth(), config.getEnvironmentHeight());
        physicsWorld = new World(new Vec2());
        placementArea = new PlacementArea(environment.getWidth(), environment.getHeight());
        placementArea.setSeed(config.getSimulationSeed());
        schedule.reset();
        System.gc();

        physicsWorld.setContactListener(contactListener);

        // Create ALL the objects
        createWalls();
        createTargetArea();
        robotFactory
                .placeInstances(placementArea.new ForType<>(), physicsWorld, config.getObjectsRobots(),
                        config.getTargetAreaPlacement());
        resourceFactory.placeInstances(placementArea.new ForType<>(), physicsWorld,
                config.getLargeObjects(), config.getSmallObjects());


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
        int environmentWidth = config.getEnvironmentWidth();
        int environmentHeight = config.getEnvironmentHeight();
        // Left
        Double2D pos = new Double2D(0, environmentHeight / 2.0);
        Double2D v1 = new Double2D(0, -pos.y);
        Double2D v2 = new Double2D(0, pos.y);
        WallObject wall = new WallObject(physicsWorld, pos, v1, v2);
        environment.setObjectLocation(wall.getPortrayal(), pos);

        // Right
        pos = new Double2D(environmentWidth, environmentHeight / 2.0);
        wall = new WallObject(physicsWorld, pos, v1, v2);
        environment.setObjectLocation(wall.getPortrayal(), pos);

        // Top
        pos = new Double2D(environmentWidth / 2.0, 0);
        v1 = new Double2D(-pos.x, 0);
        v2 = new Double2D(pos.x, 0);
        wall = new WallObject(physicsWorld, pos, v1, v2);
        environment.setObjectLocation(wall.getPortrayal(), pos);

        // Bottom
        pos = new Double2D(environmentWidth / 2.0, environmentHeight);
        wall = new WallObject(physicsWorld, pos, v1, v2);
        environment.setObjectLocation(wall.getPortrayal(), pos);
    }

    //create target area
    private void createTargetArea() {
        int environmentWidth = config.getEnvironmentWidth();
        int environmentHeight = config.getEnvironmentHeight();

        final int width, height;
        final Double2D pos;

        if (config.getTargetAreaPlacement() == SimConfig.Direction.NORTH) {
            width = environmentWidth;
            height = config.getTargetAreaThickness();
            pos = new Double2D(width/2,height/2);
        } else if (config.getTargetAreaPlacement() == SimConfig.Direction.SOUTH) {
            width = environmentWidth;
            height = config.getTargetAreaThickness();
            pos = new Double2D(environmentWidth - width/2, environmentHeight - height/2);
        } else if (config.getTargetAreaPlacement() == SimConfig.Direction.EAST) {
            width = config.getTargetAreaThickness();
            height = environmentHeight;
            pos = new Double2D(environmentWidth - width/2, height/2);
        } else if (config.getTargetAreaPlacement() == SimConfig.Direction.WEST) {
            width = config.getTargetAreaThickness();
            height = environmentHeight;
            pos = new Double2D(width/2,height/2);
        } else {
            return; // Don't know where to place this target area
        }

        //for now just give it the default fitness function
        targetArea = new TargetAreaObject(physicsWorld, pos, width, height,
                                                            config.getFitnessFunction());

        // Add target area to placement area (trust that space returned since nothing else placed
        // yet).
        PlacementArea.Space space = placementArea.getSpaceAtPosition(width, height, pos);
        placementArea.placeObject(space, targetArea);
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);
        config.setSimulationSeed(seed);
    }

    /** Get the environment (forage area) for this simulation. */
    public Continuous2D getEnvironment() {
        return environment;
    }

    /**
     * Run the simulation for the number of iterations specified in the config.
     */
    public void run() {
        final int iterations = config.getSimulationIterations();
        runForNIterations(iterations);
    }

    /**
     * Run the simulation for a certain number of iterations.
     * @param n the number of iterations
     */
    public void runForNIterations(int n) {
        start();
        for (int i = 0; i < n; i++) {
            schedule.step(this);
        }
        finish();
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
