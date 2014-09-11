package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.object.ResourceObject;

import java.util.Map;

/**
 * Factory for resource objects
 * Created by jamie on 2014/08/21.
 */
public interface ResourceFactory {
    void placeInstances(PlacementArea.ForType<ResourceObject> placementArea, World world);

    void configure(Map<String, Object> resourceConfigs);
}
