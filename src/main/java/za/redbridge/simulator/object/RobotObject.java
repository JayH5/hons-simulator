package za.redbridge.simulator.object;

import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;

import sim.util.Double2D;
import za.redbridge.simulator.portrayal.OvalPortrayal2D;
import za.redbridge.simulator.portrayal.ShapePortrayal2D;

/**
 * Object that represents the agents in the environment.
 *
 * All AgentObjects are round with a fixed radius.
 *
 * Created by jamie on 2014/07/23.
 */
public class RobotObject extends PhysicalObject {

    private final double radius;

    public RobotObject(double mass, double radius, Double2D position) {
        super(mass, new OvalPortrayal2D(radius * 2, radius * 2), position);
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    protected RectangularShape createCollisionShape() {
        return new Ellipse2D.Double(0.0, 0.0, radius * 2, radius * 2);
    }
}
