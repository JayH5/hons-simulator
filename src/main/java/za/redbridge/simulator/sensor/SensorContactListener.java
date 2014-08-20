package za.redbridge.simulator.sensor;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.TargetAreaObject;

/**
 * The contact listener for sensor intersections
 * Created by jamie on 2014/08/13.
 */
public class SensorContactListener implements ContactListener {

    public SensorContactListener() {
    }

    @Override
    public void beginContact(Contact contact) {
        handleContact(contact, true);
    }

    @Override
    public void endContact(Contact contact) {
        handleContact(contact, false);
    }

    /**
     * Handle the beginning or end of a contact.
     * @param contact the contact
     * @param begin true if the contact has begun, false if it has ended
     */
    private void handleContact(Contact contact, boolean begin) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.getBody() == fixtureB.getBody()) {
            return; // Shouldn't happen
        }

        boolean aIsSensor = fixtureA.isSensor();
        boolean bIsSensor = fixtureB.isSensor();

        // Both are sensor fixtures, check if one is not a robot sensor while one is
        final Fixture sensorFixture, objectFixture;
        if (aIsSensor) {
            if (bIsSensor) { // Both sensors of some kind
                if (fixtureA.getUserData() instanceof Sensor) {
                    if (!(fixtureB.getUserData() instanceof Sensor)) {
                        // fixtureA is agent sensor, fixtureB is some other sensor
                        sensorFixture = fixtureA;
                        objectFixture = fixtureB;
                    } else {
                        return; // Both sensors are agent sensors
                    }
                } else if (fixtureB.getUserData() instanceof Sensor) {
                    // fixtureB is agent sensor, fixtureA is some other sensor
                    sensorFixture = fixtureB;
                    objectFixture = fixtureA;
                } else {
                    return; // Both sensors are not agent sensors
                }
            } else {
                sensorFixture = fixtureA;
                objectFixture = fixtureB;
            }
        } else if (bIsSensor) {
            sensorFixture = fixtureB;
            objectFixture = fixtureA;
        } else {
            return; // Neither are sensors, nothing to sense
        }

        final Object sensorData = sensorFixture.getUserData();
        if (sensorData != null && sensorData instanceof Sensor) {
            Sensor sensor = (Sensor) sensorData;
            if (begin) {
                sensor.addFixtureInField(objectFixture);
            } else {
                sensor.removeFixtureInField(objectFixture);
            }
        } else {
            Object sensorBodyData = sensorFixture.getBody().getUserData();
            if (sensorBodyData != null && sensorBodyData instanceof TargetAreaObject) {
                TargetAreaObject targetArea = (TargetAreaObject) sensorBodyData;

                Object objectBodyData = objectFixture.getBody().getUserData();
                //update value of total objects collected
                if (objectBodyData != null && objectBodyData instanceof ResourceObject) {
                    ResourceObject resourceObject = (ResourceObject) objectBodyData;
                    if (begin) {
                        targetArea.addResource(resourceObject);
                    } else {
                        targetArea.removeResource(resourceObject);
                    }
                }
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {
        // NO-OP
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {
        // NO-OP
    }
}
