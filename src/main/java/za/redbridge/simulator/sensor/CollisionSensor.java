package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by jamie on 2014/08/05.
 */
public class CollisionSensor extends Sensor {

    private final List<Double> readings = new ArrayList<>(1);
    private static final float DEFAULT_RANGE = 10.0f; //make sure this is > robot radius

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

    protected static class GeneralSensedObject extends SensedObject{
        protected Vec2 position;
        public GeneralSensedObject(PhysicalObject object, double dist, Vec2 position) {
            super(object, dist, 0.0, 0.0);
            this.position = position;
        }

        public Vec2 getPosition() {
            return position;
        }
    }

    @Override
    protected SensedObject senseFixture(Fixture fixture, Transform sensorTransform) {
        Transform objectTransform = fixture.getBody().getTransform();
        Transform objectRelativeTransform = Transform.mulTrans(sensorTransform, objectTransform);

        Vec2 distNormal = new Vec2();
        double dist = fixture.computeDistance(getBody().getPosition(), 0, distNormal);

        if(dist > range) return null;
        Vec2 them = distNormal.mul((float)-dist); //negated because the normal is given from the fixture to the sensor
        Rot r = objectRelativeTransform.q;
        Rot.mulToOut(r,them,them);
        PhysicalObject object = (PhysicalObject) fixture.getBody().getUserData();
        return new GeneralSensedObject(object, dist, them);
    }

    @Override
    protected SensorReading provideReading(List<SensedObject> objects) {
        Optional<GeneralSensedObject> closest = objects.stream()
                .min((a,b) -> new Double(a.getDistance()).compareTo(b.getDistance()))
                .map(o -> (GeneralSensedObject)o);

        readings.clear();
        closest.ifPresent(o -> readings.add((double)o.getPosition().x));
        closest.ifPresent(o -> readings.add((double)o.getPosition().y));
        return new SensorReading(readings);
    }

    protected double readingCurve(double fraction) {
        // Sigmoid proximity response
        final double offset = 0.5;
        return 1 / (1 + Math.exp(fraction + offset));
    }

    @Override
    public boolean isRelevantObject (Fixture fixture) {
        return !(fixture.getUserData() instanceof Sensor) &&
                !(fixture.getBody().getUserData() instanceof TargetAreaObject);
    }
}
