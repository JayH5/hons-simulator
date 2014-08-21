package za.redbridge.simulator.object;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.awt.Color;
import java.awt.Paint;

import sim.util.Double2D;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;


import static za.redbridge.simulator.Utils.toVec2;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject extends PhysicalObject {

    private static final Paint DEFAULT_COLOUR = new Color(255, 235, 82);

    private final double value;

    public ResourceObject(World world, Double2D position, double width, double height, double mass,
                          double value) {
        super(createPortrayal(width, height),
                createBody(world, position, width, height, mass));
        this.value = value;
    }

    protected static Portrayal createPortrayal(double width, double height) {
        return new RectanglePortrayal(width, height, DEFAULT_COLOUR, true);
    }

    protected static Body createBody(World world, Double2D position, double width, double height,
                                     double mass) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.DYNAMIC)
                .setPosition(toVec2(position))
                .setRectangular((float) width, (float) height)
                .setDensity((float) (mass / (width * height)))
                .setFriction(0.9f)
                .setRestitution(1.0f)
                .build(world);
    }

    public double getValue() {
        return value;
    }

}
