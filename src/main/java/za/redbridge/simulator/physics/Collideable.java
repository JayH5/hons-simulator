package za.redbridge.simulator.physics;

import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Interface for objects that wish to be notified when they collide with other objects.
 * Created by jamie on 2014/08/25.
 */
public interface Collideable {

    void handleBeginContact(Contact contact, Fixture otherFixture);

    void handleEndContact(Contact contact, Fixture otherFixture);

}
