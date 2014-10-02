package za.redbridge.simulator.sensor;

import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import java.util.List;
import java.util.Optional;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.physics.FilterConstants;


/**
 * A sensor that picks up the closest relevant object and determines the distance to that object as
 * well as the position on the edge of that object that is closest to the sensor.
 * Created by jamie on 2014/08/05.
 */
public abstract class ClosestObjectSensor
        extends Sensor<Optional<ClosestObjectSensor.ClosestObject>> {

    public ClosestObjectSensor() {
    }

    @Override
    protected int getFilterCategoryBits() {
        return FilterConstants.CategoryBits.HEURISTIC_SENSOR;
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
        // Find the closest object
        ClosestObject closestObject = null;
        double closestDistance = Double.MAX_VALUE;
        for (Fixture fixture : fixtures) {
            ClosestObject object = senseFixture(fixture);
            double distance = object.getDistance();
            if (distance < closestDistance) {
                closestObject = object;
                closestDistance = distance;
            }
        }
        return Optional.ofNullable(closestObject);
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
