package za.redbridge.simulator.interfaces;

import org.jbox2d.dynamics.World;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.object.RobotObject;

import java.util.List;

public interface RobotFactory {
    void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world, int quantity);
}
