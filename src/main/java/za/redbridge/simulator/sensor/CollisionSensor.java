package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;

/**
 * Created by jamie on 2014/09/10.
 */
public class CollisionSensor extends ClosestObjectSensor {

    private static final float DEFAULT_RANGE = 0.15f + 0.4f; //make sure this is > robot radius

    private final float range;

    public CollisionSensor(float range) {
        this.range = range;
        //setDrawEnabled(true);
    }

    public CollisionSensor() {
        this(DEFAULT_RANGE);
    }

    @Override
    protected Transform createTransform(RobotObject robot) {
        return new Transform(); // Centered, not rotated
    }

    @Override
    protected Shape createShape(Transform transform) {
        Shape shape = new CircleShape();
        shape.setRadius(range);
        return shape;
    }

    @Override
    protected Portrayal createPortrayal() {
        return new CirclePortrayal(range, DEFAULT_PAINT, true);
    }

    @Override
    protected boolean filterOutObject(PhysicalObject object) {
        if (object instanceof ResourceObject) {
            ResourceObject resource = (ResourceObject) object;
            // Filter out resources that are neither collected nor pushed by max robots
            return !resource.isCollected() && !resource.pushedByMaxRobots();
        } else if (object instanceof RobotObject) {
            RobotObject robot = (RobotObject) object;
            // Filter out robots bound to resources
            return robot.isBoundToResource();
        }
        return false;
    }

    @Override
    public boolean isRelevantObject(PhysicalObject object) {
        return !(object instanceof TargetAreaObject);
    }
}
