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

    /**
     * Place all the resources within the provided placement area.
     * @param placementArea A placement area for ResourceObjects
     * @param world the JBox2D world that the object will be created in
     */
    void placeInstances(PlacementArea.ForType<ResourceObject> placementArea, World world);

    void configure(Map<String, Object> resourceConfigs);

    /**
     * @return the number of resources this resource factory will place
     */
    int getNumberOfResources();

    /**
     * @return the total value of all the resources placed by this factory
     */
    public double getTotalResourceValue();
}
