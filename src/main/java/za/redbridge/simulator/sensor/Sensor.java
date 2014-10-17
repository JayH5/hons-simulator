package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.physics.Collideable;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.STRTransform;

/**
 * The base class for all sensors that attach to a RobotObject.
 * NOTE: Sensors do not detect other sensors.
 *
 * Created by xenos on 8/22/14.
 * @param <T> the type that this Sensor returns from the {@link #sense()} method
 */
public abstract class Sensor<T> implements Collideable {
    protected static final Paint DEFAULT_PAINT = new Color(100, 100, 100, 50);

    private Portrayal portrayal;
    private Fixture sensorFixture;

    private Transform robotRelativeTransform;

    private boolean cachedSensorTransformValid;
    private final Transform cachedSensorTransform = new Transform();
    private final Transform cachedObjectRelativeTransform = new Transform();

    private final List<Fixture> sensedFixtures = new ArrayList<>();

    public Sensor() {
    }

    public final T sense() {
        if (sensorFixture == null) {
            throw new IllegalStateException("Sensor not attached, cannot sense");
        }

        // Update the paint of the portrayal
        if (portrayal != null) {
            portrayal.setPaint(getPaint());
        }

        // Invalidate the cached transform
        cachedSensorTransformValid = false;

        List<Fixture> fixtures = new ArrayList<>(sensedFixtures.size());
        for (Fixture fixture : sensedFixtures) {
            PhysicalObject obj = (PhysicalObject) fixture.getBody().getUserData();
            if (!filterOutObject(obj)) {
                fixtures.add(fixture);
            }
        }

        return provideReading(fixtures);
    }

    /**
     * Get this sensor's global transform. NOTE: this transform is cached. If you change the value
     * of the returned Transform object very bad things will happen.
     * @return this sensor's global transform
     */
    protected final Transform getSensorTransform() {
        if (!cachedSensorTransformValid) {
            Transform robotTransform = sensorFixture.getBody().getTransform();
            Transform.mulToOut(robotTransform, robotRelativeTransform, cachedSensorTransform);
            cachedSensorTransformValid = true;
        }
        return cachedSensorTransform;
    }

    /**
     * Get the fixture's transform relative to this sensor. NOTE: this transform is cached. The same
     * Transform object is used for every Fixture passed to this method.
     * @param fixture the fixture
     * @return the fixture's body's transform relative to this sensor
     */
    protected final Transform getFixtureRelativeTransform(Fixture fixture) {
        Transform objectTransform = fixture.getBody().getTransform();
        Transform sensorTransform = getSensorTransform();

        Transform.mulTransToOut(sensorTransform, objectTransform, cachedObjectRelativeTransform);
        return cachedObjectRelativeTransform;
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

        // Set the filter flags
        fixtureDef.filter.categoryBits = getFilterCategoryBits();
        fixtureDef.filter.maskBits = getFilterMaskBits();

        // Set a negative group index for all sensors so that they never collide
        fixtureDef.filter.groupIndex = FilterConstants.GroupIndexes.SENSOR;

        // Attach
        sensorFixture = robot.getBody().createFixture(fixtureDef);

        // Create the portrayal
        portrayal = createPortrayal();

        // Make sure the portrayal is relative to the robot
        if (portrayal != null) {
            STRTransform transform = new STRTransform(robotRelativeTransform);
            portrayal.setLocalTransform(transform);
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
     * Get the filter category bits for the fixture for this sensor.
     */
    protected abstract int getFilterCategoryBits();

    /**
     * Get the filter mask bits for the fixture for this sensor.
     * @return The bits for the objects that this sensor should collide with.
     */
    protected abstract int getFilterMaskBits();

    /**
     * Create the portrayal for this sensor (i.e. it's visualization). The portrayal need not be
     * translated/rotated/scaled relative to the robot as this is done automatically. You may return
     * null here if a visualization is not required.
     * @return the sensor's portrayal, or null if one is not required
     */
    protected abstract Portrayal createPortrayal();

    /** Get the paint for drawing this sensor. */
    protected Paint getPaint() {
        return DEFAULT_PAINT;
    }

    protected boolean filterOutObject(PhysicalObject object) {
        return false;
    }

    /**
     * Converts a list of fixtures that have been determined to fall within the sensor's range into
     * the output of this sensor
     * @param fixtures the fixtures in the sensor's field
     * @return the reading of the objects produced by the sensor
     */
    protected abstract T provideReading(List<Fixture> fixtures);

    public final Body getBody() {
        return sensorFixture.getBody();
    }

    public final Portrayal getPortrayal() {
        return portrayal;
    }

    @Override
    public void handleBeginContact(Contact contact, Fixture otherFixture) {
        if (otherFixture.getUserData() instanceof Sensor) {
            return;
        }

        if (!sensedFixtures.contains(otherFixture)) {
            sensedFixtures.add(otherFixture);
        }
    }

    @Override
    public void handleEndContact(Contact contact, Fixture otherFixture) {
        if (otherFixture.getUserData() instanceof Sensor) {
            return;
        }

        sensedFixtures.remove(otherFixture);
    }

    protected static PhysicalObject getFixtureObject(Fixture fixture) {
        return (PhysicalObject) fixture.getBody().getUserData();
    }

}
