package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.interfaces.ResourceFactory;
import za.redbridge.simulator.object.ResourceObject;

/**
 * Factory for resources where half the resources are big and half are small
 * Created by jamie on 2014/08/21.
 */
public class HalfBigHalfSmallResourceFactory implements ResourceFactory {

    private static final double SMALL_OBJECT_WIDTH = 0.4;
    private static final double SMALL_OBJECT_HEIGHT = 0.4;
    private static final double SMALL_OBJECT_MASS = 2.0;
    private static final int SMALL_OBJECT_PUSHING_BOTS = 1;

    private static final double LARGE_OBJECT_WIDTH = 0.6;
    private static final double LARGE_OBJECT_HEIGHT = 0.6;
    private static final double LARGE_OBJECT_MASS = 5.0;
    private static final int LARGE_OBJECT_PUSHING_BOTS = 2;

    @Override
    public void placeInstances(PlacementArea.ForType<ResourceObject> placementArea, World world,
                               int quantityLarge, int quantitySmall) {

        int quantity = quantityLarge + quantitySmall;
        int small = quantity / 2;
        for (int i = 0; i < small; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomSpace(SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT, SMALL_OBJECT_MASS,
                    SMALL_OBJECT_PUSHING_BOTS);

            placementArea.placeObject(space, resource);
        }

        int big = quantity - small;
        for (int i = 0; i < big; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomSpace(LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT, LARGE_OBJECT_MASS,
                    LARGE_OBJECT_PUSHING_BOTS);

            placementArea.placeObject(space, resource);
        }
    }
}
