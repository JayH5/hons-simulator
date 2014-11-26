package za.redbridge.simulator.factories;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.object.RobotObject;

public interface RobotFactory {
    void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world,
            Vec2 targetAreaPosition);

    int getNumRobots();
    void setNumRobots(int numRobots);

}
