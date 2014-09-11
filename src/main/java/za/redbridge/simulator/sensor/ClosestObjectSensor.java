package za.redbridge.simulator.sensor;

import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;


/**
 * A sensor that picks up the closest relevant object and determines the distance to that object as
 * well as the position on the edge of that object that is closest to the sensor.
 * Created by jamie on 2014/08/05.
 */
public abstract class ClosestObjectSensor
        extends Sensor<Optional<ClosestObjectSensor.ClosestObject>> {

    public ClosestObjectSensor() {
    }

    protected ClosestObject senseFixture(Fixture fixture) {
        Transform objectRelativeTransform = getFixtureRelativeTransform(fixture);

        Vec2 vec = new Vec2();
        float dist = fixture.computeDistance(getBody().getPosition(), 0, vec);

        vec.mulLocal(-dist); // negated because the normal is given from the fixture to the sensor
        Rot.mulToOut(objectRelativeTransform.q, vec, vec);

        return new ClosestObject(getFixtureObject(fixture), dist, vec);
    }

    @Override
    protected Optional<ClosestObject> provideReading(List<Fixture> fixtures) {
        return fixtures.stream().map(this::senseFixture)
                .min(Comparator.<ClosestObject>naturalOrder());
    }

    @Override
    protected boolean filterOutObject(PhysicalObject object) {
        if (object instanceof ResourceObject) {
            ResourceObject resource = (ResourceObject) object;
            // Filter out resources that are neither collected not pushed by max robots
            return !resource.isCollected() && !resource.pushedByMaxRobots();
        } else if (object instanceof RobotObject) {
            RobotObject robot = (RobotObject) object;
            // Filter out robots bound to resources
            return robot.isBoundToResource();
        }
        return false;
    }

    public static class ClosestObject implements Comparable<ClosestObject> {

        private final PhysicalObject object;
        private final double distance;
        private final Vec2 vectorToObject;

        public ClosestObject(PhysicalObject object, double distance, Vec2 vectorToObject) {
            this.object = object;
            this.distance = distance;
            this.vectorToObject = vectorToObject;
        }

        public PhysicalObject getObject() {
            return object;
        }

        public double getDistance() {
            return distance;
        }

        public Vec2 getVectorToObject() {
            return vectorToObject;
        }

        @Override
        public int compareTo(ClosestObject o) {
            return Double.compare(distance, o.distance);
        }
    }
}