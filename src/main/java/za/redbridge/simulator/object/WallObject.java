package za.redbridge.simulator.object;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import sim.util.Double2D;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Created by jamie on 2014/08/01.
 */
public class WallObject extends PhysicalObject {

    public WallObject(World world, Double2D pos, int width, int height) {
        super(createPortrayal(width, height), createBody(world, pos, width, height));
    }

    protected static Portrayal createPortrayal(int width, int height) {
        return new RectanglePortrayal(width, height);
    }

    protected static Body createBody(World world, Double2D position, int width, int height) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.STATIC)
                .setPosition(position)
                .setRectangular(width, height)
                .setFriction(0.8f)
                .setRestitution(1f)
                .build(world);
    }
}
