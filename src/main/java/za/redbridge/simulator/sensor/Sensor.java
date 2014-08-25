package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;

import java.util.ArrayList;
import java.util.List;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.physics.Collideable;
import za.redbridge.simulator.portrayal.ConicSensorPortrayal;

/**
 * Created by xenos on 8/22/14.
 */
public abstract class Sensor implements Collideable {
    protected final boolean drawShape;

    protected final float bearing;
    protected final float orientation;
    protected final float range;

    protected ConicSensorPortrayal portrayal;
    protected Fixture sensorFixture;

    protected final Transform robotRelativeTransform = new Transform();
    protected final Transform cachedGlobalTransform = new Transform();

    protected final List<Fixture> sensedFixtures = new ArrayList<>();

    public Sensor(float bearing, float orientation, float range, boolean drawShape) {
        this.bearing = bearing;
        this.orientation = orientation;
        this.range = range;
        this.drawShape = drawShape;
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

    public void attach(RobotObject robot, float distFromCenter) {
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
        float x = (float) (Math.min(distFromCenter, robotRadius) * Math.cos(bearing));
        float y = (float) (Math.min(distFromCenter, robotRadius) * Math.sin(bearing));
        Vec2 pos = new Vec2(x, y);

        // Update transform
        robotRelativeTransform.set(pos, bearing + orientation);

        fixtureDef.shape = createShape(pos);

        // Add ourselves as user data so we can be fetched
        fixtureDef.userData = this;

        // Attach
        this.sensorFixture = robot.getBody().createFixture(fixtureDef);

        if (drawShape) {
            portrayal.setRobotRadius(robotRadius);
        }
    }

    protected abstract SensedObject senseFixture(Fixture fixture, Transform sensorTransform);

    protected abstract SensorReading provideReading(List<SensedObject> objects);

    protected abstract Shape createShape(Vec2 pos);

    @Override
    public void handleBeginContact(Contact contact, Fixture otherFixture) {
        if (!sensedFixtures.contains(otherFixture)) {
            sensedFixtures.add(otherFixture);
        }
    }

    @Override
    public void handleEndContact(Contact contact, Fixture otherFixture) {
        sensedFixtures.remove(otherFixture);
    }

    @Override
    public boolean isRelevantObject(Fixture fixture) {
        return !(fixture.getUserData() instanceof Sensor);
    }

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
