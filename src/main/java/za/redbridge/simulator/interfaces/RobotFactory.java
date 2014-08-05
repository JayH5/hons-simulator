package za.redbridge.simulator.interfaces;

import za.redbridge.simulator.object.RobotObject;

import java.util.List;

public interface RobotFactory {
    public List<RobotObject> createInstances(int number);
}
