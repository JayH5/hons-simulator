package za.redbridge.simulator.sensor;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.portrayal.ConePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.sensor.sensedobjects.CircleSensedObject;
import za.redbridge.simulator.sensor.sensedobjects.EdgeSensedObject;
import za.redbridge.simulator.sensor.sensedobjects.PolygonSensedObject;
import za.redbridge.simulator.sensor.sensedobjects.SensedObject;

import static za.redbridge.simulator.Utils.isNearlyZero;

/**
 * Describes a sensor implementation. The actual sensor is implemented in the simulator.
 */
public abstract class AgentSensor extends Sensor<SensorReading> {

    protected final float bearing;
    protected final float orientation;
    protected final float range;
    protected final float fieldOfView;

    private static final int readingSize = 1;

    private final float fovGradient;

    protected Map<String, Object> additionalConfigs = null;

    public AgentSensor() {

        bearing = 0.0f;
        orientation = 0.0f;
        range = 10.0f;
        fieldOfView = 1.5f;
        fovGradient = (float) Math.tan(fieldOfView / 2);
    }

    public AgentSensor(float bearing, float orientation, float range, float fieldOfView) {
        if (fieldOfView <= 0 || fieldOfView >= Math.PI) {
            throw new IllegalArgumentException("Invalid field of view value: " + fieldOfView);
        }

        if (range <= 0) {
            throw new IllegalArgumentException("Invalid range value: " + range);
        }

        this.bearing = bearing;
        this.orientation = orientation;
        this.range = range;
        this.fieldOfView = fieldOfView;

        fovGradient = (float) Math.tan(fieldOfView / 2);

        // Draw by default
        setDrawEnabled(true);
    }

    @Override
    protected Transform createTransform(RobotObject robot) {
        float robotRadius = robot.getRadius();

        float x = (float) (Math.cos(bearing) * robotRadius);
        float y = (float) (Math.sin(bearing) * robotRadius);

        float angle = bearing + orientation;
        return new Transform(new Vec2(x, y), new Rot(angle));
    }

    @Override
    protected Shape createShape(Transform transform) {
        Vec2[] vertices = new Vec2[3];
        vertices[0] = new Vec2();
        float xDiff = (float) (range * Math.cos(fieldOfView / 2));
        float yDiff = (float) (range * Math.sin(fieldOfView / 2));
        vertices[1] = new Vec2(xDiff, yDiff);
        vertices[2] = new Vec2(xDiff, -yDiff);

        for (int i = 0; i < 3; i++) {
            Transform.mulToOut(transform, vertices[i], vertices[i]);
        }

        PolygonShape shape = new PolygonShape();
        shape.set(vertices, 3);
        return shape;
    }

    @Override
    protected Portrayal createPortrayal() {
        return new ConePortrayal(range, fieldOfView, DEFAULT_PAINT);
    }

    @Override
    protected SensorReading provideReading(List<Fixture> fixtures) {
        return provideObjectReading(fixtures.stream()
                .map(this::senseFixture)
                .filter(o -> o != null)
                .collect(Collectors.toList()));
    }

    /**
     * Determines whether an object lies within the field of the sensor and if so where in the field
     * the object exists.
     * @param fixture the fixture to check
     * @return a {@link SensedObject} reading if the object is in the field, else null
     */
    private SensedObject senseFixture(Fixture fixture) {
        Transform objectRelativeTransform = getFixtureRelativeTransform(fixture);

        switch (fixture.getShape().getType()) {
            case CIRCLE:
                return senseCircleFixture(fixture, objectRelativeTransform);
            case POLYGON:
                return sensePolygonFixture(fixture, objectRelativeTransform);
            case EDGE:
                return senseEdgeFixture(fixture, objectRelativeTransform);
            default:
                return null;
        }
    }

    protected SensedObject senseCircleFixture(Fixture circleFixture,
            Transform objectRelativeTransform) {
        CircleShape circleShape = (CircleShape) circleFixture.getShape();

        final float x = objectRelativeTransform.p.x;
        final float y = objectRelativeTransform.p.y;
        final float radius = circleShape.getRadius();

        float y0 = y - radius;
        float y1 = y + radius;
        float x0 = x - radius;
        float x1 = x + radius;

        float yMax = fovGradient * x;
        float yMin = -yMax;

        // Ensure circle actually within FoV
        if (yMax < y0 || yMin > y1) {
            return null;
        }

        // Clamp circle width to FoV
        if (y0 < yMin) {
            x0 = lineCircleIntersection(-fovGradient, 0, x, y, radius);
            y0 = -fovGradient * x0;
        }
        if (y1 > yMax) {
            x1 = lineCircleIntersection(fovGradient, 0, x, y, radius);
            y1 = fovGradient * x1;
        }

        final double distance;
        if (y < yMin) {
            distance = Math.hypot(x0, y0);
        } else if (y > yMax) {
            distance = Math.hypot(x1, y1);
        } else {
            distance = objectRelativeTransform.p.length() - radius;
        }

        return new CircleSensedObject(getFixtureObject(circleFixture), distance, radius, x, y, x0,
                y0, x1, y1);
    }

    private float lineCircleIntersection(float m, float c, float p, float q, float r) {
        float A = m * m + 1;
        float B = 2 * (m * c - m * q - p);
        float C = q * q - r * r + p * p - 2 * c * q + c * c;

        return (-B + (float) Math.sqrt(B * B - 4 * A * C)) / (2 * A);
    }

    protected SensedObject sensePolygonFixture(Fixture polygonFixture,
            Transform objectRelativeTransform) {
        PolygonShape polygonShape = (PolygonShape) polygonFixture.getShape();

        RayCastInput rin = new RayCastInput();
        rin.maxFraction = 1f;
        rin.p2.x = range;
        RayCastOutput rout = new RayCastOutput();
        polygonShape.raycast(rout, rin, objectRelativeTransform, 0);

        // If raycast down the middle unsuccessful, try the edges of the field of view
        if (rout.fraction == 0f) {
            rin.p2.y = fovGradient * range;
            polygonShape.raycast(rout, rin, objectRelativeTransform, 0);
        }

        if (rout.fraction == 0f) {
            rin.p2.y = -rin.p2.y;
            polygonShape.raycast(rout, rin, objectRelativeTransform, 0);
        }

        if (rout.fraction == 0f) {
            // Check if sensor is inside or in contact with other object
            // If not, raycasting hit nothing
            if (!polygonFixture.testPoint(getSensorTransform().p)) {
                return null;
            }
        }

        float distance = rout.fraction * range;

        AABB aabb = new AABB();
        polygonShape.computeAABB(aabb, objectRelativeTransform, 0);
        float x0 = aabb.lowerBound.x;
        float y0 = aabb.lowerBound.y;

        if (x0 < 0) {
            x0 = 0;
        }

        float yMin = -fovGradient * x0;
        if (y0 < yMin) {
            y0 = yMin;
        }

        float x1 = aabb.upperBound.x;
        float y1 = aabb.upperBound.y;

        if (x1 > range) {
            x1 = range;
        }

        float yMax = fovGradient * x1;
        if (y1 > yMax) {
            y1 = yMax;
        }

        return new PolygonSensedObject(getFixtureObject(polygonFixture), distance, x0, y0, x1 - x0,
                y1 - y0);
    }

    protected SensedObject senseEdgeFixture(Fixture edgeFixture,
            Transform objectRelativeTransform) {
        EdgeShape edgeShape = (EdgeShape) edgeFixture.getShape();

        // Transform ends of edge to space relative to sensor
        Vec2 v1 = Transform.mul(objectRelativeTransform, edgeShape.m_vertex1);
        Vec2 v2 = Transform.mul(objectRelativeTransform, edgeShape.m_vertex2);

        // Check if vertical or horizontal line to prevent division by zero
        float dy = v2.y - v1.y;
        float dx = v2.x - v1.x;
        float x1, y1, x2, y2;
        final double distance;
        if (isNearlyZero(dy)) { // Horizontal line (shouldn't happen generally)
            if (v2.x < v1.x) {
                Vec2 temp = v2;
                v2 = v1;
                v1 = temp;
            }

            y1 = v1.y;
            y2 = v2.y;
            x1 = Math.max(y1 / fovGradient, v1.x);
            x2 = Math.min((float) Math.sqrt(range * range - y2 * y2), v2.x);

            distance = Math.hypot(x1, y1);
        } else if (isNearlyZero(dx)) { // Vertical line
            if (v2.y < v1.y) {
                Vec2 temp = v2;
                v2 = v1;
                v1 = temp;
            }

            x1 = v1.x;
            x2 = v2.x;
            y1 = Math.max(x1 * -fovGradient, v1.y);
            y2 = Math.min(x2 * fovGradient, v2.y);

            if (y1 > 0) {
                distance = Math.hypot(x1, y1); // Distance to bottom point
            } else if (y2 < 0) {
                distance = Math.hypot(x2, y2); // Distance to top point
            } else {
                distance = x1; // Distance straight to line
            }
        } else { // Other line - use line equations
            if (v2.y < v1.y) {
                Vec2 temp = v2;
                v2 = v1;
                v1 = temp;
            }

            // Get line equation for edge
            float m = dy / dx;
            float c = v1.y - m * v1.x;

            // Line-line intersection points
            x1 = (-fovGradient - c) / m;
            y1 = m * x1 + c;
            x2 = (fovGradient - c) / m;
            y2 = m * x2 + c;

            if (v1.y > y1) {
                y1 = v1.y;
                x1 = v1.x;
            }

            if (v2.y < y2) {
                y2 = v2.y;
                x2 = v2.x;
            }

            // Get equation for line perpendicular to edge passing through sensor position
            Vec2 sensorPosition = getSensorTransform().p;
            float m_ = -1 / m;
            float c_ = sensorPosition.y + 1 / m * sensorPosition.x;

            // Closest point on infinite edge is at point perpendicular line intersect with edge
            // line... but edge is not infinite
            float x = (m - m_) / (c_ - c);
            if (x > x2) {
                distance = Math.hypot(x2, y2);
            } else if (x < x1) {
                distance = Math.hypot(x1, y1);
            } else {
                float y = m * x + c;
                distance = Math.hypot(x, y);
            }
        }

        return new EdgeSensedObject(getFixtureObject(edgeFixture), distance, x1, y1, x2, y2);
    }

    /**
     * Decide whether to filter out a given PhysicalObject instance. Since the object may still
     * enter/leave the field of the sensor, we can't filter it out in
     * {@link #isRelevantObject(Fixture)} because we still want to receive updates of the object
     * leaving. We might want to filter out the object here if its state changes and it becomes
     * irrelevant while we are observing it.
     * @param object an object in the fixture list
     * @return true if the object should be ignored
     */
    protected boolean filterOutObject(PhysicalObject object) {
        return false;
    }

    /**
     * Converts a list of objects that have been determined to fall within the sensor's range into
     * an actual {@link SensorReading} instance.
     * @param objects the objects in the sensor's field, sorted by distance
     * @return the reading of the objects produced by the sensor
     */
    protected abstract SensorReading provideObjectReading(List<SensedObject> objects);

    public abstract void readAdditionalConfigs(Map<String, Object> map) throws ParseException;

    protected static boolean checkFieldPresent(Object field, String name) {
        if (field != null) {
            return true;
        }
        System.out.println("Field '" + name + "' not present, using default");
        return false;
    }

    @Override
    public abstract AgentSensor clone();

    public abstract int getReadingSize();
}
