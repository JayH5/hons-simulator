package za.redbridge.simulator.physics;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * The contact listener for sensor intersections
 * Created by jamie on 2014/08/13.
 */
public class SimulationContactListener implements ContactListener {

    public SimulationContactListener() {
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

        notifyContact(contact, fixtureA, fixtureB, begin);
        notifyContact(contact, fixtureB, fixtureA, begin);
    }

    private void notifyContact(Contact contact, Fixture fixtureA, Fixture fixtureB, boolean begin) {
        // First try notify the fixture userdata object of the collision
        // If that fails try notify the body userdata object
        final Collideable collideable;
        if (fixtureA.getUserData() instanceof Collideable) {
            collideable = (Collideable) fixtureA.getUserData();
        } else if (fixtureA.getBody().getUserData() instanceof Collideable) {
            collideable = (Collideable) fixtureA.getBody().getUserData();
        } else {
            return;
        }

        // Notify the collideable of the collision
        if (begin) {
            collideable.handleBeginContact(contact, fixtureB);
        } else {
            collideable.handleEndContact(contact, fixtureB);
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
