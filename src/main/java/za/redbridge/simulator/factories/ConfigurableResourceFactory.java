package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;
import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.config.Config;
import za.redbridge.simulator.object.ResourceObject;

import java.util.Map;

/**
 * Created by shsu on 2014/08/29.
 */
public class ConfigurableResourceFactory extends Config implements ResourceFactory {

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

    private int numSmallObjects;
    private double smallObjectWidth;
    private double smallObjectHeight;
    private double smallObjectMass;
    private int smallObjectPushingBots;

    private int numLargeObjects;
    private double largeObjectWidth;
    private double largeObjectHeight;
    private double largeObjectMass;
    private int largeObjectPushingBots;

    public ConfigurableResourceFactory(int numSmallObjects, double smallObjectWidth, double smallObjectHeight, double smallObjectMass,
                                       int smallObjectPushingBots, int numLargeObjects, double largeObjectWidth, double largeObjectHeight,
                                       double largeObjectMass, int largeObjectPushingBots) {

        this.numSmallObjects = numSmallObjects;
        this.smallObjectWidth = smallObjectWidth;
        this.smallObjectHeight = smallObjectHeight;
        this.smallObjectMass = smallObjectMass;
        this.smallObjectPushingBots = smallObjectPushingBots;

        this.numLargeObjects = numLargeObjects;
        this.largeObjectWidth = largeObjectWidth;
        this.largeObjectHeight = largeObjectHeight;
        this.largeObjectMass = largeObjectMass;
        this.largeObjectPushingBots = largeObjectPushingBots;
    }

    public ConfigurableResourceFactory() {

        this.numSmallObjects = DEFAULT_OBJECTS_RESOURCES_SMALL;
        this.smallObjectWidth = DEFAULT_SMALL_OBJECT_WIDTH;
        this.smallObjectHeight = DEFAULT_SMALL_OBJECT_HEIGHT;
        this.smallObjectMass = DEFAULT_SMALL_OBJECT_MASS;
        this.smallObjectPushingBots = DEFAULT_SMALL_OBJECT_PUSHING_BOTS;

        this.numLargeObjects = DEFAULT_OBJECTS_RESOURCES_LARGE;
        this.largeObjectWidth = DEFAULT_LARGE_OBJECT_WIDTH;
        this.largeObjectHeight = DEFAULT_LARGE_OBJECT_HEIGHT;
        this.largeObjectMass = DEFAULT_LARGE_OBJECT_MASS;
        this.largeObjectPushingBots = DEFAULT_LARGE_OBJECT_PUSHING_BOTS;
    }

    @Override
    public void placeInstances(PlacementArea.ForType<ResourceObject> placementArea, World world) {

        for (int i = 0; i < numSmallObjects; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomSpace(smallObjectWidth, smallObjectHeight);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    smallObjectWidth, smallObjectHeight, smallObjectMass,
                    smallObjectPushingBots);

            placementArea.placeObject(space, resource);
        }


        for (int i = 0; i < numLargeObjects; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomSpace(largeObjectWidth, largeObjectHeight);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    largeObjectWidth, largeObjectHeight, largeObjectMass,
                    largeObjectPushingBots);

            placementArea.placeObject(space, resource);
        }
    }

    @Override
    public void configure(Map<String, Object> resourceConfigs) {

        //Resources
        Map resourceProps = (Map) resourceConfigs.get("resourceProperties");
        if (checkFieldPresent(resourceProps, "resourceProperties")) {

            //small resources
            Integer resourcesFieldSmall = (Integer) resourceConfigs.get("smallResources");
            if (checkFieldPresent(resourcesFieldSmall, "resourceConfigs:smallResources")) {
                numSmallObjects = resourcesFieldSmall;
            }
            Double sObjWidth = (Double) resourceProps.get("smallObjectWidth");
            if (checkFieldPresent(sObjWidth, "resourceProperties:smallObjectWidth")) {
                smallObjectWidth = sObjWidth;
            }
            Double sObjHeight = (Double) resourceProps.get("smallObjectHeight");
            if (checkFieldPresent(sObjHeight, "resourceProperties:smallObjectHeight")) {
                smallObjectHeight = sObjHeight;
            }
            Double sObjMass = (Double) resourceProps.get("smallObjectMass");
            if (checkFieldPresent(sObjHeight, "resourceProperties:smallObjectMass")) {
                smallObjectMass = sObjMass;
            }
            Integer sObjPushingBots = (Integer) resourceProps.get("maxSmallObjectPushingBots");
            if (checkFieldPresent(sObjPushingBots, "resourceProperties:maxSmallObjectPushingBots")) {
                smallObjectPushingBots = sObjPushingBots;
            }

            //large resources
            Integer resourcesFieldLarge = (Integer) resourceConfigs.get("largeResources");
            if (checkFieldPresent(resourcesFieldLarge, "resourceConfigs:largeResources")) {
                numLargeObjects = resourcesFieldLarge;
            }
            Double lObjWidth = (Double) resourceProps.get("largeObjectWidth");
            if (checkFieldPresent(lObjWidth, "resourceProperties:largeObjectWidth")) {
                largeObjectWidth = lObjWidth;
            }
            Double lObjHeight = (Double) resourceProps.get("largeObjectHeight");
            if (checkFieldPresent(lObjHeight, "resourceProperties:largeObjectHeight")) {
                largeObjectHeight = lObjHeight;
            }
            Double lObjMass = (Double) resourceProps.get("largeObjectMass");
            if (checkFieldPresent(lObjHeight, "resourceProperties:largeObjectMass")) {
                largeObjectMass = lObjMass;
            }
            Integer lObjPushingBots = (Integer) resourceProps.get("maxLargeObjectPushingBots");
            if (checkFieldPresent(lObjPushingBots, "resourceProperties:maxLargeObjectPushingBots")) {
                largeObjectPushingBots = lObjPushingBots;
            }
        }
    }

}
