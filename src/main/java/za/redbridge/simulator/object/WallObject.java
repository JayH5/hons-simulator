package za.redbridge.simulator.object;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import sim.util.Double2D;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.portrayal.LinePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;

/**
 * Created by jamie on 2014/08/01.
 */
public class WallObject extends PhysicalObject {

    public WallObject(World world, Double2D pos, Double2D v1, Double2D v2) {
        super(createPortrayal(v1, v2), createBody(world, pos, v1, v2));
    }

    protected static Portrayal createPortrayal(Double2D v1, Double2D v2) {
        return new LinePortrayal(v1, v2);
    }

    protected static Body createBody(World world, Double2D position, Double2D v1, Double2D v2) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.STATIC)
                .setPosition(position)
                .setEdge(v1, v2)
                .setFriction(0.8f)
                .setRestitution(1f)
                .setFilterCategoryBits(FilterConstants.CategoryBits.WALL)
                .build(world);
    }
}
