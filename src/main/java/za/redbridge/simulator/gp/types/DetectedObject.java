package za.redbridge.simulator.gp.types;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.WallObject;

/**
 * Created by xenos on 10/3/14.
 */
public class DetectedObject {
    public enum Type { RESOURCE, ROBOT, NONE, WALL};

    public static Type fromClass(Class c) {
        if (c == ResourceObject.class) return Type.RESOURCE;
        else if (c == RobotObject.class) return Type.ROBOT;
        else if (c == WallObject.class) return Type.WALL;
        else throw new IllegalArgumentException("Could not convert class to DetectedObject");
    }
}
