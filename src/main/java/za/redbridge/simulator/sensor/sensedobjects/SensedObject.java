package za.redbridge.simulator.sensor.sensedobjects;

import java.awt.Shape;

import za.redbridge.simulator.object.PhysicalObject;

/**
 * Created by jamie on 2014/09/05.
 */
public abstract class SensedObject<T extends Shape> implements Comparable<SensedObject> {

    protected final PhysicalObject object;
    protected final double distance;

    public SensedObject(PhysicalObject object, double distance) {
        this.object = object;
        this.distance = distance;
    }

    /** Get the detected object. */
    public PhysicalObject getObject() {
        return object;
    }

    /** Get the estimated minimum distance to the object. */
    public double getDistance() {
        return distance;
    }

    /** Get the shape that describes the edge of this object as seen by the sensor. */
    public abstract T getShape();

    @Override
    public int compareTo(SensedObject o) {
        return Double.compare(distance, o.distance);
    }
}
