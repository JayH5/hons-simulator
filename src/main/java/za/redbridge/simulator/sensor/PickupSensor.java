package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import java.util.List;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Sensor to detect when object hit a certain position on the agent
 * Created by jamie on 2014/08/26.
 */
public class PickupSensor extends Sensor<ResourceObject> {

    private final float width;
    private final float height;
    private final float bearing;

    public PickupSensor(float width, float height, float bearing) {
        this.width = width;
        this.height = height;
        this.bearing = bearing;
        setDrawEnabled(true);
    }

    @Override
    protected Transform createTransform(RobotObject robot) {
        float robotRadius = robot.getRadius();
        Rot rot = new Rot(bearing);
        float x = rot.c * robotRadius;
        float y = rot.s * robotRadius;

        return new Transform(new Vec2(x, y), rot);
    }

    @Override
    protected Shape createShape(Transform transform) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2, transform.p, transform.q.getAngle());
        return shape;
    }

    @Override
    protected Portrayal createPortrayal() {
        return new RectanglePortrayal(width, height, DEFAULT_PAINT, true);
    }

    @Override
    protected ResourceObject provideReading(List<Fixture> fixtures) {
        if (!fixtures.isEmpty()) {
            return (ResourceObject) fixtures.get(0).getBody().getUserData();
        }
        return null;
    }

    @Override
    public boolean isRelevantObject(Fixture otherFixture) {
        return otherFixture.getBody().getUserData() instanceof ResourceObject;
    }
}
