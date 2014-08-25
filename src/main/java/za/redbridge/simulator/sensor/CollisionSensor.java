package za.redbridge.simulator.sensor;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import za.redbridge.simulator.object.PhysicalObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by jamie on 2014/08/05.
 */
public class CollisionSensor extends Sensor {

    private final List<Double> readings = new ArrayList<>(1);
    protected static final float RANGE = 10.0f; //make sure this is > robot radius

    public CollisionSensor() {
        this(RANGE);
    }

    public CollisionSensor(float range) {
        super(0.0f, 0.0f, range, false);
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
        Vec2 distNormal = new Vec2();
        Transform objectTransform = fixture.getBody().getTransform();
        Transform objectRelativeTransform = Transform.mulTrans(sensorTransform, objectTransform);
        fixture.getBody().getPosition();
        double dist = fixture.computeDistance(sensorFixture.getBody().getPosition(), 0, distNormal);
        if(dist > RANGE) return null;
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

    protected Shape createShape(Vec2 pos){
        Shape c = new CircleShape();
        c.setRadius(range);
        return c;
    }

    protected double readingCurve(double fraction) {
        // Sigmoid proximity response
        final double offset = 0.5;
        return 1 / (1 + Math.exp(fraction + offset));
    }
}
