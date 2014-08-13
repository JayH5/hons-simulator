package za.redbridge.simulator.object;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.portrayal.Portrayal;

/**
 * Created by jamie on 2014/07/25.
 */
public class PhysicalObject implements Steppable, Taggable {

    private String tag;

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
    }

    @Override
    public void step(SimState simState) {
        // Nothing to update if we're static
        if (body.getType() == BodyType.STATIC) {
            return;
        }

        Simulation simulation = (Simulation) simState;
        Vec2 pos = body.getPosition();
        if (pos.x < 0 || pos.x > 102 || pos.y < 0 || pos.y > 102) {
            System.out.println("Position outside of bounds: " + pos);
        }

        simulation.getEnvironment().setObjectLocation(portrayal, new Double2D(pos.x, pos.y));

        float orientation = body.getAngle();
        portrayal.setOrientation(orientation);
    }

    public Body getBody() {
        return body;
    }

    public Portrayal getPortrayal() {
        return portrayal;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
    }
}
