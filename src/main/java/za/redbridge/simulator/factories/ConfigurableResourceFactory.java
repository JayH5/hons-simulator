package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;
import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.interfaces.ResourceFactory;
import za.redbridge.simulator.object.ResourceObject;

/**
 * Created by shsu on 2014/08/29.
 */
public class ConfigurableResourceFactory implements ResourceFactory {

    private final double smallObjectWidth;
    private final double smallObjectHeight;
    private final double smallObjectMass;
    private final int smallObjectPushingBots;

    private final double largeObjectWidth;
    private final double largeObjectHeight;
    private final double largeObjectMass;
    private final int largeObjectPushingBots;

    public ConfigurableResourceFactory(double smallObjectWidth, double smallObjectHeight, double smallObjectMass,
                                       int smallObjectPushingBots, double largeObjectWidth, double largeObjectHeight,
                                       double largeObjectMass, int largeObjectPushingBots) {

        this.smallObjectWidth = smallObjectWidth;
        this.smallObjectHeight = smallObjectHeight;
        this.smallObjectMass = smallObjectMass;
        this.smallObjectPushingBots = smallObjectPushingBots;

        this.largeObjectWidth = largeObjectWidth;
        this.largeObjectHeight = largeObjectHeight;
        this.largeObjectMass = largeObjectMass;
        this.largeObjectPushingBots = largeObjectPushingBots;

    }

    @Override
    public void placeInstances(PlacementArea.ForType<ResourceObject> placementArea, World world,
                               int quantityLarge, int quantitySmall) {

        for (int i = 0; i < quantitySmall; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomSpace(smallObjectWidth, smallObjectHeight);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    smallObjectWidth, smallObjectHeight, smallObjectMass,
                    smallObjectPushingBots);

            placementArea.placeObject(space, resource);
        }


        for (int i = 0; i < quantityLarge; i++) {
            PlacementArea.Space space =
                    placementArea.getRandomSpace(largeObjectWidth, largeObjectHeight);

            ResourceObject resource = new ResourceObject(world, space.getPosition(),
                    largeObjectWidth, largeObjectHeight, largeObjectMass,
                    largeObjectPushingBots);

            placementArea.placeObject(space, resource);
        }
    }

}
