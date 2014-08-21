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

    private static final boolean AREA_VALUE = false;

    private static final double SMALL_OBJECT_WIDTH = 3;
    private static final double SMALL_OBJECT_HEIGHT = 3;
    private static final double SMALL_OBJECT_VALUE = 50.0;
    private static final double SMALL_OBJECT_MASS = 40.0;

    private static final double LARGE_OBJECT_WIDTH = 5.0;
    private static final double LARGE_OBJECT_HEIGHT = 5.0;
    private static final double LARGE_OBJECT_VALUE = 100.0;
    private static final double LARGE_OBJECT_MASS = 100.0;

    @Override
    public void placeInstances(PlacementArea.ForType<ResourceObject> placementArea, World world,
                               int quantity) {
        double value = AREA_VALUE ?
                SMALL_OBJECT_WIDTH * SMALL_OBJECT_HEIGHT : SMALL_OBJECT_VALUE;
        int small = quantity / 2;
        for (int i = 0; i < small; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomSpace(SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    SMALL_OBJECT_WIDTH, SMALL_OBJECT_HEIGHT, SMALL_OBJECT_MASS, value);

            placementArea.placeObject(space, resource);
        }

        value = AREA_VALUE ?
                LARGE_OBJECT_WIDTH * LARGE_OBJECT_HEIGHT : LARGE_OBJECT_VALUE;
        int big = quantity - small;
        for (int i = 0; i < big; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomSpace(LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    LARGE_OBJECT_WIDTH, LARGE_OBJECT_HEIGHT, LARGE_OBJECT_MASS, value);

            placementArea.placeObject(space, resource);
        }
    }
}
