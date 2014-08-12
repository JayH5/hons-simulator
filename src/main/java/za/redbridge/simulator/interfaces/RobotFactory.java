package za.redbridge.simulator.interfaces;

import org.jbox2d.dynamics.World;

import za.redbridge.simulator.object.RobotObject;

import java.util.List;

public interface RobotFactory {
    public List<RobotObject> createInstances(World world, int number);
}
