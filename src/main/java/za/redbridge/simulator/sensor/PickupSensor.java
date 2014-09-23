package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;

/**
 * Sensor to detect when object hit a certain position on the agent
 * Created by jamie on 2014/08/26.
 */
public class PickupSensor extends ClosestObjectSensor {

    private final float radius;

    public PickupSensor(float radius) {
        this.radius = radius;
        setDrawEnabled(true);
    }

    @Override
    protected Transform createTransform(RobotObject robot) {
        return new Transform();
    }

    @Override
    protected Shape createShape(Transform transform) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        return shape;
    }

    @Override
    protected Portrayal createPortrayal() {
        return new CirclePortrayal(radius, DEFAULT_PAINT, true);
    }

    @Override
    protected boolean filterOutObject(PhysicalObject object) {
        ResourceObject resource = (ResourceObject) object;
        // Filter out resources that can't be picked up
        return !resource.canBePickedUp();
    }

    @Override
    public boolean isRelevantObject(PhysicalObject object) {
        return object instanceof ResourceObject;
    }
}
