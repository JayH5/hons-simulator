package za.redbridge.simulator.khepera;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.dynamics.Fixture;

import java.text.ParseException;
import java.util.Map;

import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.sensedobjects.PolygonSensedObject;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;


import static za.redbridge.simulator.physics.AABBUtil.testPoint;

/**
 * The Khepera III robots feature two IR proximity sensors facing the ground near the front of the
 * robot. These are typically used for line-tracking tasks. Here we instead treat the two sensors
 * as one sensor that is able to detect the target area. If the ground in the target area were
 * painted a different colour to the rest of the environment then these bottom-down sensors could
 * realistically be used to detect when the agent is in the target area.
 *
 * Created by jamie on 2014/10/01.
 */
public class BottomProximitySensor extends ProximitySensor {

    private static final float FIELD_RADIUS = 0.05f;

    private RobotObject robot;

    public BottomProximitySensor() {
        super(0f, 0f);
    }

    @Override
    protected Transform createTransform(RobotObject robot) {
        this.robot = robot;
        Transform transform = new Transform();
        transform.p.set(robot.getRadius() * 0.95f, 0f); // Place near the front of the agent
        return transform;
    }

    @Override
    protected Shape createShape(Transform transform) {
        CircleShape shape = new CircleShape();
        shape.setRadius(FIELD_RADIUS);
        shape.m_p.set(transform.p);
        return shape;
    }

    @Override
    protected int getFilterCategoryBits() {
        return FilterConstants.CategoryBits.TARGET_AREA_SENSOR;
    }

    @Override
    protected int getFilterMaskBits() {
        return FilterConstants.CategoryBits.TARGET_AREA;
    }

    @Override
    protected Portrayal createPortrayal() {
        return new CirclePortrayal(FIELD_RADIUS, DEFAULT_PAINT, true);
    }

    @Override
    public void readAdditionalConfigs(Map<String, Object> map) throws ParseException {

    }

    @Override
    public AgentSensor clone() {
        return new BottomProximitySensor();
    }

    @Override
    public int getReadingSize() {
        return 1;
    }

    @Override
    public Map<String, Object> getAdditionalConfigs() {
        return null;
    }

    @Override
    protected SensedObject sensePolygonFixture(Fixture polygonFixture,
            Transform objectRelativeTransform) {
        // Make a fast path since this sensor can only detect the target area
        TargetAreaObject targetArea = (TargetAreaObject) getFixtureObject(polygonFixture);
        AABB aabb = targetArea.getAabb();
        Transform sensorTransform = getSensorTransform();

        // Check if sensor directly above target area
        if (testPoint(sensorTransform.p, aabb)) {
            final float x, y, w, h;
            if (sensorTransform.p.x + FIELD_RADIUS > aabb.upperBound.x) {
                x = -FIELD_RADIUS;
                y = -FIELD_RADIUS;
                w = FIELD_RADIUS + aabb.upperBound.x - sensorTransform.p.x;
                h = FIELD_RADIUS * 2;
            } else if (sensorTransform.p.x - FIELD_RADIUS < aabb.lowerBound.x) {
                x = aabb.lowerBound.x - sensorTransform.p.x;
                y = -FIELD_RADIUS;
                w = FIELD_RADIUS + -x;
                h = FIELD_RADIUS * 2;
            } else if (sensorTransform.p.y + FIELD_RADIUS > aabb.upperBound.y) {
                x = -FIELD_RADIUS;
                y = -FIELD_RADIUS;
                w = FIELD_RADIUS * 2;
                h = FIELD_RADIUS + aabb.upperBound.y - sensorTransform.p.y;
            } else if (sensorTransform.p.y - FIELD_RADIUS < aabb.upperBound.y) {
                x = -FIELD_RADIUS;
                y = aabb.lowerBound.y - sensorTransform.p.y;
                w = FIELD_RADIUS - x;
                h = FIELD_RADIUS + -y;
            } else {
                x = -FIELD_RADIUS;
                y = -FIELD_RADIUS;
                w = h = FIELD_RADIUS * 2;
            }

            return new PolygonSensedObject(targetArea, 0f, x, y, w, h);
        }

        final float distance, x, y, w, h;
        if (sensorTransform.p.x < aabb.lowerBound.x) {
            distance = aabb.lowerBound.x - sensorTransform.p.x;
            x = distance;
            y = -FIELD_RADIUS;
            w = FIELD_RADIUS - distance;
            h = FIELD_RADIUS * 2;
        } else if (sensorTransform.p.x > aabb.upperBound.x) {
            distance = sensorTransform.p.x - aabb.upperBound.x;
            x = -distance;
            y = -FIELD_RADIUS;
            w = FIELD_RADIUS - distance;
            h = FIELD_RADIUS * 2;
        } else if (sensorTransform.p.y < aabb.lowerBound.y) {
            distance = aabb.lowerBound.y - sensorTransform.p.y;
            x = -FIELD_RADIUS;
            y = distance;
            w = FIELD_RADIUS * 2;
            h = FIELD_RADIUS - distance;
        } else if (sensorTransform.p.y > aabb.upperBound.y) {
            distance = sensorTransform.p.y - aabb.upperBound.y;
            x = -FIELD_RADIUS;
            y = -distance;
            w = FIELD_RADIUS * 2;
            h = FIELD_RADIUS - distance;
        } else {
            // Shouldn't happen
            return null;
        }

        return new PolygonSensedObject(targetArea, distance, x, y, w, h);
    }

    @Override
    protected double readingCurve(float distance) {
        // Assume that the sensor is 2.5mm above the ground so that its reading peaks when the
        // target area is exactly below it
        return super.readingCurve(distance + 0.0025f);
    }

}
