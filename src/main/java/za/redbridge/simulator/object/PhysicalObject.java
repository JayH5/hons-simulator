package za.redbridge.simulator.object;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

import sim.engine.SimState;
import sim.engine.Steppable;
import za.redbridge.simulator.portrayal.Portrayal;

/**
 * Created by jamie on 2014/07/25.
 */
public class PhysicalObject implements Steppable {

    private final Portrayal portrayal;
    private final Body body;

    public PhysicalObject(Portrayal portrayal, Body body) {
        if (portrayal == null || body == null) {
            throw new NullPointerException("Portrayal and body must not be null");
        }

        this.portrayal = portrayal;
        this.body = body;

        // Make this body trackable
        this.body.setUserData(this);

        // Make sure we're drawn in the right place
        this.portrayal.setTransform(this.body.getTransform());
    }

    @Override
    public void step(SimState simState) {
        // Nothing to update if we're static or sleeping
        if (body.getType() == BodyType.STATIC || !body.isAwake()) {
            return;
        }

        portrayal.setTransform(body.getTransform());
    }

    public Body getBody() {
        return body;
    }

    public Portrayal getPortrayal() {
        return portrayal;
    }
}
