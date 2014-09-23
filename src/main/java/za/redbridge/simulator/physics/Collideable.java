package za.redbridge.simulator.physics;

import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

import za.redbridge.simulator.object.PhysicalObject;

/**
 * Interface for objects that wish to be notified when they collide with other objects.
 * Created by jamie on 2014/08/25.
 */
public interface Collideable {

    void handleBeginContact(Contact contact, Fixture otherFixture);

    void handleEndContact(Contact contact, Fixture otherFixture);

    /**
     * Check whether the colliding object is relevant to this Collideable. This should return a
     * value that does not change. i.e. for a given object, this method must ALWAYS return the
     * same value. If an object is relevant then this Collideable will receive calls to
     * {@link #handleBeginContact(Contact, Fixture)} and {@link #handleEndContact(Contact, Fixture)}
     * for the fixtures of the object, otherwise not.
     * @param object the colliding object
     * @return true if the object is relevant
     */
    boolean isRelevantObject(PhysicalObject object);
}
