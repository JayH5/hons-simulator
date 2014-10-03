package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.object.ResourceObject;

import java.util.Map;

/**
 * Factory for resources where half the resources are big and half are small
 * Created by jamie on 2014/08/21.
 */
public class HalfBigHalfSmallResourceFactory implements ResourceFactory {


    private static final int DEFAULT_OBJECTS_RESOURCES_SMALL = 20;
    private static final float SMALL_OBJECT_WIDTH = 0.4f;
    private static final float SMALL_OBJECT_HEIGHT = 0.4f;
    private static final float SMALL_OBJECT_MASS = 2.0f;
    private static final int SMALL_OBJECT_PUSHING_BOTS = 1;

    private static final int DEFAULT_OBJECTS_RESOURCES_LARGE = 20;
    private static final float LARGE_OBJECT_WIDTH = 0.6f;
    private static final float LARGE_OBJECT_HEIGHT = 0.6f;
    private static final float LARGE_OBJECT_MASS = 5.0f;
    private static final int LARGE_OBJECT_PUSHING_BOTS = 2;

    @Override
    public void placeInstances(PlacementArea.ForType<ResourceObject> placementArea, World world) {

        int quantity = DEFAULT_OBJECTS_RESOURCES_LARGE + DEFAULT_OBJECTS_RESOURCES_SMALL;
        int small = quantity / 2;
        for (int i = 0; i < small; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomRectangularSpace(SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    space.getAngle(), SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT, SMALL_OBJECT_MASS,
                    SMALL_OBJECT_PUSHING_BOTS);

            placementArea.placeObject(space, resource);
        }

        int big = quantity - small;
        for (int i = 0; i < big; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomRectangularSpace(LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    space.getAngle(), LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT, LARGE_OBJECT_MASS,
                    LARGE_OBJECT_PUSHING_BOTS);

            placementArea.placeObject(space, resource);
        }
    }

    @Override
    public void configure(Map<String, Object> resourceConfigs) {}

    @Override
    public int getNumberOfResources() {
        return DEFAULT_OBJECTS_RESOURCES_LARGE + DEFAULT_OBJECTS_RESOURCES_SMALL;
    }
}
