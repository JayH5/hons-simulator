package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Sensor to detect when object hit a certain position on the agent
 * Created by jamie on 2014/08/26.
 */
public class PickupSensor extends ClosestObjectSensor {

    private final float width;
    private final float height;

    public PickupSensor(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected Transform createTransform(RobotObject robot) {
        Transform transform = new Transform();
        transform.p.set(robot.getRadius() + width / 2, 0);
        return transform;
    }

    @Override
    protected Shape createShape(Transform transform) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        return shape;
    }

    @Override
    protected int getFilterMaskBits() {
        return FilterConstants.CategoryBits.RESOURCE;
    }

    @Override
    protected Portrayal createPortrayal() {
        return new RectanglePortrayal(width, height, DEFAULT_PAINT, true);
    }

    @Override
    protected boolean filterOutObject(PhysicalObject object) {
        ResourceObject resource = (ResourceObject) object;
        // Filter out resources that can't be picked up
        return !resource.canBePickedUp();
    }

}
