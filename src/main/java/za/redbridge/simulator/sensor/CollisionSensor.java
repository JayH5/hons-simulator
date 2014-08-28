package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;


/**
 * Created by jamie on 2014/08/05.
 */
public class CollisionSensor extends Sensor<Optional<Vec2>> {

    private static final float DEFAULT_RANGE = 0.15f + 0.05f; //make sure this is > robot radius

    private final float range;

    public CollisionSensor(float range) {
        this.range = range;
        setDrawEnabled(true);
    }

    public CollisionSensor() {
        this(DEFAULT_RANGE);
    }

    @Override
    protected Transform createTransform(RobotObject robot) {
        return new Transform(); // Centered, not rotated
    }

    @Override
    protected Shape createShape(Transform transform) {
        Shape shape = new CircleShape();
        shape.setRadius(range);
        return shape;
    }

    @Override
    protected Portrayal createPortrayal() {
        return new CirclePortrayal(range, DEFAULT_PAINT, true);
    }

    protected SensedCollision senseFixture(Fixture fixture) {
        Transform objectRelativeTransform = getFixtureRelativeTransform(fixture);

        Vec2 distNormal = new Vec2();
        double dist = fixture.computeDistance(getBody().getPosition(), 0, distNormal);

        if(dist > range) {
            return null;
        }

        Vec2 them = distNormal.mul((float)-dist); //negated because the normal is given from the fixture to the sensor
        Rot r = objectRelativeTransform.q;
        Rot.mulToOut(r, them, them);
        return new SensedCollision(dist, them);
    }

    @Override
    protected Optional<Vec2> provideReading(List<Fixture> fixtures) {
        Optional<SensedCollision> closest = fixtures.stream()
                .map(this::senseFixture)
                .filter(o -> o != null)
                .min(Comparator.<SensedCollision>naturalOrder());

        return closest.map(SensedCollision::getPosition);
    }

    @Override
    public boolean isRelevantObject (Fixture fixture) {
        return !(fixture.getUserData() instanceof Sensor) &&
                !(fixture.getBody().getUserData() instanceof TargetAreaObject) &&
                !(fixture.getBody().getUserData() instanceof ResourceObject);

    }

    protected static class SensedCollision implements Comparable<SensedCollision> {

        private final double distance;
        private final Vec2 position;

        public SensedCollision(double distance, Vec2 position) {
            this.distance = distance;
            this.position = position;
        }

        public double getDistance() {
            return distance;
        }

        public Vec2 getPosition() {
            return position;
        }

        @Override
        public int compareTo(SensedCollision o) {
            return Double.compare(distance, o.distance);
        }
    }
}
