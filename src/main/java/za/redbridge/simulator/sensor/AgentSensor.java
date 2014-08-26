package za.redbridge.simulator.sensor;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import java.util.List;
import java.util.stream.Collectors;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.portrayal.ConePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;

/**
 * Describes a sensor implementation. The actual sensor is implemented in the simulator.
 */
public abstract class AgentSensor extends Sensor<SensorReading> {

    protected final float bearing;
    protected final float orientation;
    protected final float range;
    protected final float fieldOfView;

    private final float fovGradient;

    public AgentSensor(float bearing, float orientation, float range, float fieldOfView) {
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
     * @return a {@link za.redbridge.simulator.sensor.AgentSensor.SensedObject} reading if the object is in the field, else null
     */
    protected SensedObject senseFixture(Fixture fixture) {
        Transform objectRelativeTransform = getFixtureRelativeTransform(fixture);

        Shape shape = fixture.getShape();
        final float objectY0, objectY1, objectDistance;
        if (shape.getType() == ShapeType.CIRCLE) {
            objectY0 = objectRelativeTransform.p.y - shape.getRadius();
            objectY1 = objectRelativeTransform.p.y + shape.getRadius();
            objectDistance = (float) Math.hypot(objectRelativeTransform.p.x, objectRelativeTransform.p.y);
        } else if (shape.getType() == ShapeType.POLYGON) {
            RayCastInput rin = new RayCastInput();
            rin.maxFraction = 1f;
            rin.p2.x = range;
            RayCastOutput rout = new RayCastOutput();
            shape.raycast(rout, rin, objectRelativeTransform, 0);

            // If raycast down the middle unsuccessful, try the edges of the field of view
            if (rout.fraction == 0f) {
                rin.p2.y = fovGradient * range;
                shape.raycast(rout, rin, objectRelativeTransform, 0);
            }

            if (rout.fraction == 0f) {
                rin.p2.y = -rin.p2.y;
                shape.raycast(rout, rin, objectRelativeTransform, 0);
            }

            if (rout.fraction == 0f) {
                // Check if sensor is inside or in contact with other object
                // If not, raycasting hit nothing
                if (!fixture.testPoint(getSensorTransform().p)) {
                   return null;
                }
            }

            objectDistance = rout.fraction * range;

            AABB aabb = new AABB();
            shape.computeAABB(aabb, objectRelativeTransform, 0);
            objectY0 = aabb.lowerBound.y;
            objectY1 = aabb.upperBound.y;
        } else {
            // Don't know this shape
            return null;
        }

        // Boundaries of field of view obey equation y = mx + c
        // Where: m = (+/-) fovGradient, c = 0
        // We can get the symmetrical span across the y-axis of the field of view of the sensor for
        // a distance x from the sensor.
        double y1 = objectDistance / fovGradient;
        double y0 = -y1;

        // Check if object within field at all
        if (objectY1 < y0 || objectY0 > y1) {
            return null;
        }

        // Clamp span to field of sensor
        double spanStart = objectY0 > y0 ? objectY0 : y0;
        double spanEnd = objectY1 < y1 ? objectY1 : y1;

        PhysicalObject object = (PhysicalObject) fixture.getBody().getUserData();
        return new SensedObject(object, objectDistance, spanStart, spanEnd);
    }

    /**
     * Converts a list of objects that have been determined to fall within the sensor's range into
     * an actual {@link SensorReading} instance.
     * @param objects the objects in the sensor's field, sorted by distance
     * @return the reading of the objects produced by the sensor
     */
    protected abstract SensorReading provideObjectReading(List<SensedObject> objects);

    /**
     * Container class for intermediary sensor readings - contains the object to be sensed and
     * information about its location.
     */
    protected static class SensedObject implements Comparable<SensedObject> {
        private final PhysicalObject object;
        private final double spanStart;
        private final double spanEnd;
        private final double distance;

        public SensedObject(PhysicalObject object, double dist, double spanStart, double spanEnd) {
            this.object = object;
            this.distance = dist;
            this.spanStart = spanStart;
            this.spanEnd = spanEnd;
        }

        /** Get the detected object. */
        public PhysicalObject getObject() {
            return object;
        }

        /** Get the estimated distance to the object. */
        public double getDistance() {
            return distance;
        }

        /** Get the start of the object's coverage of the field of view */
        public double getSpanStart() {
            return spanStart;
        }

        /** Get the end of the object's coverage of the field of view */
        public double getSpanEnd() {
            return spanEnd;
        }

        @Override
        public int compareTo(SensedObject o) {
            return Double.compare(distance, o.distance);
        }
    }

}
