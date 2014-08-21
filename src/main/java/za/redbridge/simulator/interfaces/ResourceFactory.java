package za.redbridge.simulator.interfaces;

import org.jbox2d.dynamics.World;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.object.ResourceObject;

/**
 * Factory for resource objects
 * Created by jamie on 2014/08/21.
 */
public interface ResourceFactory {
    void placeInstances(PlacementArea.ForType<ResourceObject> placementArea, World world,
            int quantity);
}
