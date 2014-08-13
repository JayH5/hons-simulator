package za.redbridge.simulator.object;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import sim.util.Double2D;
import sim.util.Int2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

import java.awt.*;

import static za.redbridge.simulator.Utils.toVec2;

/**
 * Created by shsu on 2014/08/13.
 */
public class TargetAreaObject extends PhysicalObject {

        public TargetAreaObject(World world, Double2D pos, int width, int height) {
            super(createPortrayal(width, height), createBody(world, pos, width, height));
        }

        protected static Portrayal createPortrayal(int width, int height) {
            Paint areaColour = new Color(31, 110, 11, 100);
            return new RectanglePortrayal(width, height, areaColour, true);
        }

        protected static Body createBody(World world, Double2D position, int width, int height) {
            BodyBuilder bb = new BodyBuilder();
            return bb.setBodyType(BodyType.STATIC)
                    .setPosition(toVec2(position))
                    .setRectangular(width, height)
                    .setFriction(0.0f)
                    .setRestitution(1.0f)
                    .build(world);
        }

}
