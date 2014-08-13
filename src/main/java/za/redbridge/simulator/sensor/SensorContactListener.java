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
        //System.out.println("Begin contact");
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        boolean aIsSensor = fixtureA.isSensor();
        boolean bIsSensor = fixtureB.isSensor();

        if (aIsSensor && bIsSensor) {
            return;
        }

        final Fixture sensorFixture, objectFixture;
        if (aIsSensor) {
            sensorFixture = fixtureA;
            objectFixture = fixtureB;
        } else {
            sensorFixture = fixtureB;
            objectFixture = fixtureA;
        }

        Object userData = sensorFixture.getUserData();
        Object objectData = objectFixture.getUserData();

        if (userData == null)
            return;

        if (userData instanceof Sensor) {
            Sensor sensor = (Sensor) userData;
            sensor.addFixtureInField(objectFixture);
        }
        else if (userData instanceof TargetAreaObject) {
            TargetAreaObject targetArea = (TargetAreaObject) userData;

            //update value of total objects collected
            if (objectData instanceof ResourceObject){
                ResourceObject resourceObject = (ResourceObject) objectData;
                targetArea.addResource(resourceObject);
            }
        }

    }

    @Override
    public void endContact(Contact contact) {
        //System.out.println("End contact");
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        boolean aIsSensor = fixtureA.isSensor();
        boolean bIsSensor = fixtureB.isSensor();

        if (aIsSensor && bIsSensor) {
            return;
        }

        final Fixture sensorFixture, objectFixture;
        if (aIsSensor) {
            sensorFixture = fixtureA;
            objectFixture = fixtureB;
        } else {
            sensorFixture = fixtureB;
            objectFixture = fixtureA;
        }

        Object userData = sensorFixture.getUserData();
        if (userData == null || !(userData instanceof Sensor)) {
            return;
        }

        Sensor sensor = (Sensor) userData;
        sensor.removeFixtureInField(objectFixture);
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
