package za.redbridge.simulator.sensor;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import za.redbridge.simulator.object.PhysicalObject;
import za.redbridge.simulator.portrayal.ConicSensorPortrayal;

/**
 * Describes a sensor implementation. The actual sensor is implemented in the simulator.
 */
public abstract class AgentSensor extends Sensor {
    private static final Paint PAINT = new Color(100, 100, 100, 100);
    protected float fieldOfView;
    protected final float fovGradient;

    public AgentSensor(float bearing, float orientation, float range, boolean drawShape, float fieldOfView) {
        super(bearing, orientation, range, drawShape);
        this.fieldOfView = fieldOfView;

        fovGradient = (float) Math.tan(fieldOfView / 2);

        if (drawShape) {
            portrayal = new ConicSensorPortrayal(bearing, orientation, range, fieldOfView, PAINT);
        }
    }

    public void draw(Graphics2D graphics) {
        if (drawShape) {
            portrayal.setPaint(getPaint());
            portrayal.draw(null, graphics, null);
        }
    }

    protected Paint getPaint() {
        return PAINT;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    @Override
    public Shape createShape(Vec2 pos){
        Vec2[] vertices = new Vec2[3];
        vertices[0] = pos;
        float xDiff = (float) (range * Math.cos(fieldOfView / 2));
        float yDiff = (float) (range * Math.sin(fieldOfView / 2));
        vertices[1] = new Vec2(pos.x + xDiff, pos.y + yDiff);
        vertices[2] = new Vec2(pos.x + xDiff, pos.y - yDiff);

        PolygonShape shape = new PolygonShape();
        shape.set(vertices, 3);
        return shape;
    }

    /**
     * Determines whether an object lies within the field of the sensor and if so where in the field
     * the object exists.
     * @param fixture the fixture to check
     * @return a {@link SensedObject} reading if the object is in the field, else null
     */
    protected SensedObject senseFixture(Fixture fixture, Transform sensorTransform) {
        Transform objectTransform = fixture.getBody().getTransform();
        Transform objectRelativeTransform = Transform.mulTrans(sensorTransform, objectTransform);

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
                if (!fixture.testPoint(sensorTransform.p)) {
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
    protected abstract SensorReading provideReading(List<SensedObject> objects);

}
