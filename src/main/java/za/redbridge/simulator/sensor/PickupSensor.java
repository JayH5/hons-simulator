package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import java.util.List;
import java.util.Optional;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Sensor to detect when object hit a certain position on the agent
 * Created by jamie on 2014/08/26.
 */
public class PickupSensor extends Sensor<Optional<ResourceObject>> {

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
        float x = rot.c * robotRadius + width / 2;
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
    protected Optional<ResourceObject> provideReading(List<Fixture> fixtures) {
        if (!fixtures.isEmpty()) {
            // Rectangle/rectangle collision - we can cheat and check if three corners of the
            // sensor are within the other rectangle
            Fixture fixture = fixtures.get(0);
            Vec2 testPoint = new Vec2();
            if (testLocalPoint(0, 0, testPoint, fixture)
                    && testLocalPoint(width, height, testPoint, fixture)
                    && testLocalPoint(width, 0, testPoint, fixture)) {
                return Optional.of((ResourceObject) fixture.getBody().getUserData());
            }
        }
        return Optional.empty();
    }

    private boolean testLocalPoint(float x, float y, Vec2 point, Fixture otherFixture) {
        point.set(x, y);
        Transform.mulToOut(getSensorTransform(), point, point);
        return otherFixture.testPoint(point);
    }

    @Override
    public boolean isRelevantObject(Fixture otherFixture) {
        return otherFixture.getBody().getUserData() instanceof ResourceObject;
    }
}
