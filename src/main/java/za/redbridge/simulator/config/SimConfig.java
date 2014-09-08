package za.redbridge.simulator.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import za.redbridge.simulator.ea.DefaultFitnessFunction;
import za.redbridge.simulator.ea.FitnessFunction;

public class SimConfig extends Config {

    private static final long DEFAULT_SIMULATION_SEED = System.currentTimeMillis();
    private static final int DEFAULT_SIMULATION_ITERATIONS = 10000;
    private static final int DEFAULT_ENVIRONMENT_WIDTH = 20;
    private static final int DEFAULT_ENVIRONMENT_HEIGHT = 20;
    private static final int DEFAULT_TARGET_AREA_THICKNESS = (int)(DEFAULT_ENVIRONMENT_HEIGHT * 0.2);
    private static final Direction DEFAULT_TARGET_AREA_PLACEMENT = Direction.SOUTH;
    private static final int DEFAULT_OBJECTS_ROBOTS = 10;
    private static final int DEFAULT_OBJECTS_RESOURCES_LARGE = 20;
    private static final int DEFAULT_OBJECTS_RESOURCES_SMALL = 20;

    private static final double DEFAULT_SMALL_OBJECT_WIDTH = 0.4;
    private static final double DEFAULT_SMALL_OBJECT_HEIGHT = 0.4;
    private static final double DEFAULT_SMALL_OBJECT_MASS = 5.0;
    private static final int DEFAULT_SMALL_OBJECT_PUSHING_BOTS = 1;

    private static final double DEFAULT_LARGE_OBJECT_WIDTH = 0.6;
    private static final double DEFAULT_LARGE_OBJECT_HEIGHT = 0.6;
    private static final double DEFAULT_LARGE_OBJECT_MASS = 15.0;
    private static final int DEFAULT_LARGE_OBJECT_PUSHING_BOTS = 2;

    private static final FitnessFunction DEFAULT_FITNESS_FUNCTION = new DefaultFitnessFunction();

    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    private long simulationSeed;
    private final int simulationIterations;

    private final int environmentWidth;
    private final int environmentHeight;


    private final int objectsRobots;
    private final int objectsResourcesLarge;
    private final int objectsResourcesSmall;

    private final Direction targetAreaPlacement;
    private final int targetAreaThickness;

    private final double smallObjectWidth;
    private final double smallObjectHeight;
    private final double smallObjectMass;
    private final int smallObjectPushingBots;

    private final double largeObjectWidth;
    private final double largeObjectHeight;
    private final double largeObjectMass;
    private final int largeObjectPushingBots;

    private final FitnessFunction fitnessFunction;

    //default config
    public SimConfig() {
        this(DEFAULT_SIMULATION_SEED, DEFAULT_SIMULATION_ITERATIONS, DEFAULT_ENVIRONMENT_WIDTH,
                DEFAULT_ENVIRONMENT_HEIGHT, DEFAULT_TARGET_AREA_PLACEMENT,
                DEFAULT_TARGET_AREA_THICKNESS, DEFAULT_OBJECTS_ROBOTS, DEFAULT_OBJECTS_RESOURCES_LARGE, DEFAULT_OBJECTS_RESOURCES_SMALL,
                DEFAULT_SMALL_OBJECT_WIDTH, DEFAULT_SMALL_OBJECT_HEIGHT, DEFAULT_SMALL_OBJECT_MASS, DEFAULT_SMALL_OBJECT_PUSHING_BOTS,
                DEFAULT_LARGE_OBJECT_WIDTH, DEFAULT_LARGE_OBJECT_HEIGHT, DEFAULT_LARGE_OBJECT_MASS, DEFAULT_LARGE_OBJECT_PUSHING_BOTS,
                DEFAULT_FITNESS_FUNCTION);
    }

    public SimConfig(long simulationSeed, int simulationIterations,
                     int environmentWidth, int environmentHeight,
                     Direction targetAreaPlacement, int targetAreaThickness,
                     int objectsRobots, int objectsResourcesLarge, int objectsResourcesSmall, double smallObjectWidth,
                     double smallObjectHeight, double smallObjectMass, int smallObjectPushingBots,
                     double largeObjectWidth, double largeObjectHeight, double largeObjectMass,
                     int largeObjectPushingBots, FitnessFunction fitnessFunction) {
        this.simulationSeed = simulationSeed;
        this.simulationIterations = simulationIterations;

        this.environmentWidth = environmentWidth;
        this.environmentHeight = environmentHeight;

        this.targetAreaPlacement = targetAreaPlacement;
        this.targetAreaThickness = targetAreaThickness;

        this.objectsRobots = objectsRobots;
        this.objectsResourcesLarge = objectsResourcesLarge;
        this.objectsResourcesSmall = objectsResourcesSmall;

        this.smallObjectWidth = smallObjectWidth;
        this.smallObjectHeight = smallObjectHeight;
        this.smallObjectMass = smallObjectMass;
        this.smallObjectPushingBots = smallObjectPushingBots;

        this.largeObjectWidth = largeObjectWidth;
        this.largeObjectHeight = largeObjectHeight;
        this.largeObjectMass = largeObjectMass;
        this.largeObjectPushingBots = largeObjectPushingBots;

        this.fitnessFunction = fitnessFunction;

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
        int resourcesLarge = DEFAULT_OBJECTS_RESOURCES_LARGE;
        int resourcesSmall = DEFAULT_OBJECTS_RESOURCES_SMALL;

        double smallObjWidth = DEFAULT_SMALL_OBJECT_WIDTH;
        double smallObjHeight = DEFAULT_SMALL_OBJECT_HEIGHT;
        double smallObjMass = DEFAULT_SMALL_OBJECT_MASS;
        int smallObjPushingBots = DEFAULT_SMALL_OBJECT_PUSHING_BOTS;

        double largeObjWidth = DEFAULT_LARGE_OBJECT_WIDTH;
        double largeObjHeight = DEFAULT_LARGE_OBJECT_HEIGHT;
        double largeObjMass = DEFAULT_LARGE_OBJECT_MASS;
        int largeObjPushingBots = DEFAULT_LARGE_OBJECT_PUSHING_BOTS;

        FitnessFunction fitness = DEFAULT_FITNESS_FUNCTION;

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
            Integer resourcesFieldLarge = (Integer) objects.get("largeResources");
            if (checkFieldPresent(resourcesFieldLarge, "objects:largeResources")) {
                resourcesLarge = resourcesFieldLarge;
            }
            Integer resourcesFieldSmall = (Integer) objects.get("smallResources");
            if (checkFieldPresent(resourcesFieldSmall, "objects:smallResources")) {
                resourcesSmall = resourcesFieldSmall;
            }
        }

        //Resources
        Map resourceProps = (Map) config.get("resourceProperties");
        if (checkFieldPresent(resourceProps, "resourceProperties")) {
            Double sObjWidth = (Double) resourceProps.get("smallObjectWidth");
            if (checkFieldPresent(sObjWidth, "resourceProperties:smallObjectWidth")) {
                smallObjWidth = sObjWidth;
            }
            Double sObjHeight = (Double) resourceProps.get("smallObjectHeight");
            if (checkFieldPresent(sObjHeight, "resourceProperties:smallObjectHeight")) {
                smallObjHeight = sObjHeight;
            }
            Double sObjMass = (Double) resourceProps.get("smallObjectMass");
            if (checkFieldPresent(sObjHeight, "resourceProperties:smallObjectMass")) {
                smallObjMass = sObjMass;
            }
            Integer sObjPushingBots = (Integer) resourceProps.get("maxSmallObjectPushingBots");
            if (checkFieldPresent(sObjPushingBots, "resourceProperties:maxSmallObjectPushingBots")) {
                smallObjPushingBots = sObjPushingBots;
            }

            Double lObjWidth = (Double) resourceProps.get("largeObjectWidth");
            if (checkFieldPresent(lObjWidth, "resourceProperties:largeObjectWidth")) {
                largeObjWidth = lObjWidth;
            }
            Double lObjHeight = (Double) resourceProps.get("largeObjectHeight");
            if (checkFieldPresent(lObjHeight, "resourceProperties:largeObjectHeight")) {
                largeObjHeight = lObjHeight;
            }
            Double lObjMass = (Double) resourceProps.get("largeObjectMass");
            if (checkFieldPresent(lObjHeight, "resourceProperties:largeObjectMass")) {
                largeObjMass = lObjMass;
            }
            Integer lObjPushingBots = (Integer) resourceProps.get("maxLargeObjectPushingBots");
            if (checkFieldPresent(lObjPushingBots, "resourceProperties:maxLargeObjectPushingBots")) {
                largeObjPushingBots = lObjPushingBots;
            }
        }

        // Fitness function
        Map fitnessFunc = (Map) config.get("scoring");
        if (checkFieldPresent(fitnessFunc, "scoring")) {
            String fitnessF = (String) fitnessFunc.get("fitnessFunction");
            if (checkFieldPresent(fitnessF, "scoring:fitnessFunction")) {

                try {
                    Class f = Class.forName(fitnessF);
                    Object o = f.newInstance();

                    if (!(o instanceof FitnessFunction)) {
                        throw new InvalidClassException("");
                    }

                    fitness = (FitnessFunction) o;
                }
                catch (ClassNotFoundException c) {
                    System.out.println("Invalid class name specified in SimConfig: " + fitnessF + ". Using default fitness function.");
                    c.printStackTrace();
                }
                catch (InvalidClassException i) {
                    System.out.println("Invalid specified fitness class. " + fitnessF + ". Using default fitness function.");
                    i.printStackTrace();
                }
                catch (InstantiationException ins) {
                    ins.printStackTrace();
                }
                catch (IllegalAccessException ill) {
                    ill.printStackTrace();
                }
            }
        }

        return new SimConfig(seed, iterations, width, height, placement, thickness, robots,
                resourcesLarge, resourcesSmall, smallObjWidth, smallObjHeight, smallObjMass, smallObjPushingBots,
                largeObjWidth, largeObjHeight, largeObjMass, largeObjPushingBots, fitness);
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

    public int getLargeObjects() {
        return objectsResourcesLarge;
    }

    public int getSmallObjects() {
        return objectsResourcesSmall;
    }

    public double getSmallObjectWidth() {
        return smallObjectWidth;
    }

    public double getSmallObjectHeight() {
        return smallObjectHeight;
    }

    public double getSmallObjectMass() {
        return smallObjectMass;
    }

    public int getSmallObjectPushingBots() {
        return smallObjectPushingBots;
    }

    public double getLargeObjectWidth() {
        return largeObjectWidth;
    }

    public double getLargeObjectHeight() {
        return largeObjectHeight;
    }

    public double getLargeObjectMass() {
        return largeObjectMass;
    }

    public int getLargeObjectPushingBots() {
        return largeObjectPushingBots;
    }

    public FitnessFunction getFitnessFunction() { return fitnessFunction; }

}
