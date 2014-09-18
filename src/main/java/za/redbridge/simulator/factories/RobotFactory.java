package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;

import java.util.List;
import java.util.Map;

public interface RobotFactory {
    void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world,
                        SimConfig.Direction targetAreaPlacement);

    int getNumRobots();
    void setNumRobots(int numRobots);

}
