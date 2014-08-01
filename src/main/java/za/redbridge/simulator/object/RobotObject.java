package za.redbridge.simulator.object;

import java.awt.Paint;

import sim.physics2D.shape.Circle;
import sim.physics2D.util.Angle;
import sim.util.Double2D;

/**
 * Object that represents the agents in the environment.
 *
 * All AgentObjects are round with a fixed radius.
 *
 * Created by jamie on 2014/07/23.
 */
public class RobotObject extends PhysicalObject {

    public RobotObject(Double2D position, double mass, double radius, Paint paint) {
        setPose(position, new Angle(0));
        setShape(new Circle(radius, paint), mass);

        setCoefficientOfFriction(0);
        setCoefficientOfRestitution(1);
    }

}
