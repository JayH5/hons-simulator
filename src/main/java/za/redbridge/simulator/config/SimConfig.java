package za.redbridge.simulator.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class SimConfig {

    private static final long DEFAULT_SIMULATION_SEED = System.currentTimeMillis();
    private static final int DEFAULT_SIMULATION_ITERATIONS = 10000;
    private static final int DEFAULT_ENVIRONMENT_WIDTH = 100;
    private static final int DEFAULT_ENVIRONMENT_HEIGHT = 100;
    private static final int DEFAULT_TARGET_AREA_THICKNESS = 20;
    private static final Direction DEFAULT_TARGET_AREA_PLACEMENT = Direction.SOUTH;
    private static final int DEFAULT_OBJECTS_ROBOTS = 15;
    private static final int DEFAULT_OBJECTS_RESOURCES = 10;

    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    private long simulationSeed;
    private final int simulationIterations;

    private final int environmentWidth;
    private final int environmentHeight;


    private final int objectsRobots;
    private final int objectsResources;


    private final Direction targetAreaPlacement;
    private final int targetAreaThickness;


    //default config
    public SimConfig() {
        this(DEFAULT_SIMULATION_SEED, DEFAULT_SIMULATION_ITERATIONS, DEFAULT_ENVIRONMENT_WIDTH,
                DEFAULT_ENVIRONMENT_HEIGHT, DEFAULT_TARGET_AREA_PLACEMENT,
                DEFAULT_TARGET_AREA_THICKNESS, DEFAULT_OBJECTS_ROBOTS, DEFAULT_OBJECTS_RESOURCES);
    }

    public SimConfig(long simulationSeed, int simulationIterations,
                     int environmentWidth, int environmentHeight,
                     Direction targetAreaPlacement, int targetAreaThickness,
                     int objectsRobots, int objectsResources) {
        this.simulationSeed = simulationSeed;
        this.simulationIterations = simulationIterations;

        this.environmentWidth = environmentWidth;
        this.environmentHeight = environmentHeight;

        this.targetAreaPlacement = targetAreaPlacement;
        this.targetAreaThickness = targetAreaThickness;

        this.objectsRobots = objectsRobots;
        this.objectsResources = objectsResources;
    }

    @SuppressWarnings("unchecked")
    public static SimConfig loadFromFile(String filepath) {
        Yaml yaml = new Yaml();
        Map<String, Object> config;
        try (Reader reader = Files.newBufferedReader(Paths.get(filepath))) {
            config = (Map<String, Object>) yaml.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // This is fairly horrible
        long seed = DEFAULT_SIMULATION_SEED;
        int iterations = DEFAULT_SIMULATION_ITERATIONS;
        int width = DEFAULT_ENVIRONMENT_WIDTH;
        int height = DEFAULT_ENVIRONMENT_HEIGHT;
        Direction placement = DEFAULT_TARGET_AREA_PLACEMENT;
        int thickness = DEFAULT_TARGET_AREA_THICKNESS;
        int robots = DEFAULT_OBJECTS_ROBOTS;
        int resources = DEFAULT_OBJECTS_RESOURCES;

        // Load simulation
        Map simulation = (Map) config.get("simulation");
        if (checkFieldPresent(simulation, "simulation")) {
            Number seedField = (Number) simulation.get("seed");
            if (checkFieldPresent(seedField, "simulation:seed")) {
                seed = seedField.longValue();
            }
            Integer iterationsField = (Integer) simulation.get("iterations");
            if (checkFieldPresent(iterationsField, "simulation:iterations")) {
                iterations = iterationsField;
            }
        }

        // Environment
        Map environment = (Map) config.get("environment");
        if (checkFieldPresent(environment, "environment")) {
            Integer widthField = (Integer) environment.get("width");
            if (checkFieldPresent(widthField, "environment:width")) {
                width = widthField;
            }
            Integer heightField = (Integer) environment.get("height");
            if (checkFieldPresent(heightField, "environment:height")) {
                height = heightField;
            }
        }

        // Target area
        Map targetArea = (Map) config.get("targetArea");
        if (checkFieldPresent(targetArea, "targetArea")) {
            String placementField = (String) targetArea.get("placement");
            if (checkFieldPresent(placementField, "targetArea:placement")) {
                placement = Direction.valueOf(placementField.toUpperCase());
            }
            Integer thicknessField = (Integer) targetArea.get("thickness");
            if (checkFieldPresent(thicknessField, "targetArea:thickness")) {
                thickness = thicknessField;
            }
        }

        // Objects
        Map objects = (Map) config.get("objects");
        if (checkFieldPresent(objects, "objects")) {
            Integer robotsField = (Integer) objects.get("robots");
            if (checkFieldPresent(robotsField, "objects:robots")) {
                robots = robotsField;
            }
            Integer resourcesField = (Integer) objects.get("resources");
            if (checkFieldPresent(resourcesField, "objects:resources")) {
                resources = resourcesField;
            }
        }

        return new SimConfig(seed, iterations, width, height, placement, thickness, robots,
                resources);
    }

    private static boolean checkFieldPresent(Object field, String name) {
        if (field != null) {
            return true;
        }
        System.out.println("Field '" + name + "' not present, using default");
        return false;
    }

    public long getSimulationSeed() {
        return simulationSeed;
    }

    public void setSimulationSeed(long seed) {
        this.simulationSeed = seed;
    }

    public int getSimulationIterations() {
        return simulationIterations;
    }

    public int getEnvironmentWidth() {
        return environmentWidth;
    }

    public int getEnvironmentHeight() {
        return environmentHeight;
    }

    public Direction getTargetAreaPlacement() {
        return targetAreaPlacement;
    }

    public int getTargetAreaThickness() {
        return targetAreaThickness;
    }

    public int getObjectsRobots() {
        return objectsRobots;
    }

    public int getObjectsResources() {
        return objectsResources;
    }

}
