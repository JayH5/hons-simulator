package za.redbridge.simulator.sensor;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.portrayal.SensorPortrayal;

/**
 * Describes a sensor implementation. The actual sensor is implemented in the simulator.
 */
public abstract class Sensor {

    private static final Paint PAINT = new Color(100, 100, 100, 100);
    private static final boolean DRAW_SHAPE = true;

    protected final float bearing;
    protected final float orientation;
    protected final float range;
    protected final float fieldOfView;

    private final float fovGradient;

    private SensorPortrayal portrayal;
    private Fixture sensorFixture;

    private final List<Fixture> sensedFixtures = new ArrayList<>();

    private final Transform robotRelativeTransform = new Transform();
    private final Transform cachedGlobalTransform = new Transform();

    public Sensor(float bearing, float orientation, float range, float fieldOfView) {
        this.bearing = bearing;
        this.orientation = orientation;
        this.range = range;
        this.fieldOfView = fieldOfView;

        fovGradient = (float) Math.tan(fieldOfView / 2);

        if (DRAW_SHAPE) {
            portrayal = new SensorPortrayal(bearing, orientation, range, fieldOfView, PAINT);
        }
    }

    public void attach(RobotObject robot) {
        // Clear existing fixture
        if (sensorFixture != null) {
            sensorFixture.destroy();
            sensorFixture = null;

            // Clear the list of objects in case there are any strays
            sensedFixtures.clear();
        }

        // Create a fixture definition
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = true;
        fixtureDef.friction = 0f;

        // Calculate position relative to robot
        float robotRadius = robot.getRadius();
        float x = (float) (robotRadius * Math.cos(bearing));
        float y = (float) (robotRadius * Math.sin(bearing));
        Vec2 pos = new Vec2(x, y);

        // Update transform
        robotRelativeTransform.set(pos, bearing + orientation);

        // Create a shape
        Vec2[] vertices = new Vec2[3];
        vertices[0] = pos;
        float xDiff = (float) (range * Math.cos(fieldOfView / 2));
        float yDiff = (float) (range * Math.sin(fieldOfView / 2));
        vertices[1] = new Vec2(x + xDiff, y + yDiff);
        vertices[2] = new Vec2(x + xDiff, y - yDiff);

        PolygonShape shape = new PolygonShape();
        shape.set(vertices, 3);
        fixtureDef.shape = shape;

        // Add ourselves as user data so we can be fetched
        fixtureDef.userData = this;

        // Attach
        this.sensorFixture = robot.getBody().createFixture(fixtureDef);

        if (DRAW_SHAPE) {
            portrayal.setRobotRadius(robotRadius);
        }
    }

    public SensorReading sense() {
        if (sensorFixture == null) {
            throw new IllegalStateException("Sensor not attached, cannot sense");
        }

        Transform robotTransform = sensorFixture.getBody().getTransform();
        Transform.mulToOut(robotTransform, robotRelativeTransform, cachedGlobalTransform);

        List<SensedObject> sensedObjects = new ArrayList<>();
        for (Fixture f : sensedFixtures) {
            SensedObject obj = senseFixture(f, cachedGlobalTransform);
            if (obj != null) {
                sensedObjects.add(obj);
            }
        }

        return provideReading(sensedObjects);
    }

    public void draw(Graphics2D graphics) {
        if (DRAW_SHAPE) {
            portrayal.setPaint(getPaint());
            portrayal.draw(null, graphics, null);
        }
    }

    protected Paint getPaint() {
        return PAINT;
    }

    public double getBearing() {
        return bearing;
    }

    public double getOrientation() {
        return orientation;
    }

    public double getRange() {
        return range;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    void addFixtureInField(Fixture fixture) {
        sensedFixtures.add(fixture);
    }

    void removeFixtureInField(Fixture fixture) {
        sensedFixtures.remove(fixture);
    }

    /**
     * Determines whether an object lies within the field of the sensor and if so where in the field
     * the object exists.
     * @param fixture the fixture to check
     * @return a {@link SensedObject} reading if the object is in the field, else null
     */
    private SensedObject senseFixture(Fixture fixture, Transform sensorTransform) {
        Transform objectTransform = fixture.getBody().getTransform();
        Transform objectRelativeTransform = Transform.mulTrans(sensorTransform, objectTransform);

        Shape shape = fixture.getShape();
        final float objectY0, objectY1, objectDistance;
        if (shape instanceof CircleShape) {
            objectY0 = objectRelativeTransform.p.y - shape.getRadius();
            objectY1 = objectRelativeTransform.p.y + shape.getRadius();
            objectDistance = objectRelativeTransform.p.x;
        } else if (shape instanceof PolygonShape) {
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
                return null; // Not much we can do
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
    protected abstract SensorReading provideReading(List<SensedObject> objects);

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

        /** Get the object that has been sensed */
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
