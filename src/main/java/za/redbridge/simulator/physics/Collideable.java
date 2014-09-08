package za.redbridge.simulator.physics;

import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Created by jamie on 2014/08/25.
 */
public interface Collideable {

    void handleBeginContact(Contact contact, Fixture otherFixture);

    void handleEndContact(Contact contact, Fixture otherFixture);

    boolean isRelevantObject(Fixture otherFixture);
}
