package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import sim.portrayal.DrawInfo2D;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.physics.Collideable;
import za.redbridge.simulator.portrayal.Portrayal;

/**
 * Created by xenos on 8/22/14.
 */
public abstract class Sensor implements Collideable {
    protected static final Paint DEFAULT_PAINT = new Color(100, 100, 100, 100);

    private boolean drawEnabled = false;

    private Portrayal portrayal;
    private Fixture sensorFixture;

    private Transform robotRelativeTransform;
    private final Transform cachedGlobalTransform = new Transform();

    private final List<Fixture> sensedFixtures = new ArrayList<>();

    public Sensor() {
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

    public final void attach(RobotObject robot) {
        // Clear existing fixture
        if (sensorFixture != null) {
            sensorFixture.destroy();
            sensorFixture = null;

            // Clear the list of objects in case there are any strays
            sensedFixtures.clear();
        }

        // Update transform
        robotRelativeTransform = createTransform(robot);

        // Create a fixture definition
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = true;

        fixtureDef.shape = createShape(robotRelativeTransform);

        // Add ourselves as user data so we can be fetched
        fixtureDef.userData = this;

        // Attach
        sensorFixture = robot.getBody().createFixture(fixtureDef);
        Shape shape = new CircleShape();

        // Create the portrayal
        portrayal = createPortrayal();

        // Make sure the portrayal is relative to the robot
        if (portrayal != null) {
            AffineTransform at = new AffineTransform();
            at.translate(robotRelativeTransform.p.x, robotRelativeTransform.p.y);
            at.rotate(robotRelativeTransform.q.getAngle());
            portrayal.setTransformOverride(at);
        }
    }

    /**
     * Create the transform for this sensor relative to the provided robot
     * @param robot the robot this sensor is attached to
     * @return the transform relative to the robot
     */
    protected abstract Transform createTransform(RobotObject robot);

    /**
     * Create the shape for this sensor relative to the robot's center.
     * @param transform the shape vertices must be transformed by this transform
     * @return the shape of this sensor
     */
    protected abstract Shape createShape(Transform transform);

    /**
     * Create the portrayal for this sensor (i.e. it's visualization). The portrayal need not be
     * translated/rotated/scaled relative to the robot as this is done automatically. You may return
     * null here if a visualization is not required.
     * @return the sensor's portrayal, or null if one is not required
     */
    protected abstract Portrayal createPortrayal();

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        if (drawEnabled && portrayal != null) {
            portrayal.setPaint(getPaint());
            portrayal.draw(object, graphics, info);
        }
    }

    /** Get the paint for drawing this sensor. */
    protected Paint getPaint() {
        return DEFAULT_PAINT;
    }

    protected abstract SensedObject senseFixture(Fixture fixture, Transform sensorTransform);

    protected abstract SensorReading provideReading(List<SensedObject> objects);

    public final Body getBody() {
        return sensorFixture.getBody();
    }

    public final Portrayal getPortrayal() {
        return portrayal;
    }

    /** Check whether drawing of this sensor is enabled. */
    public boolean isDrawEnabled() {
        return drawEnabled;
    }

    /** Set whether this sensor should be drawn. */
    public void setDrawEnabled(boolean drawEnabled) {
        this.drawEnabled = drawEnabled;
    }

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
