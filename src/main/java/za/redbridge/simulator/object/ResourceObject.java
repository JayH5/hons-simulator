package za.redbridge.simulator.object;

import java.awt.Paint;

import sim.engine.SimState;
import sim.physics2D.shape.Rectangle;
import sim.physics2D.util.Angle;
import sim.util.Double2D;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject extends MobileObject {

    private final double value;

    public ResourceObject(Double2D position, double mass, double width, double height, Paint paint,
            double value) {
        setPose(position, new Angle(0));
        setShape(new Rectangle(width, height, paint), mass);
        setCoefficientOfFriction(0.2);
        setCoefficientOfStaticFriction(0.5);
        setVelocity(new Double2D(0,0));
        setAngularVelocity(0.0);
        this.value = value;
    }

    @Override
    public void step(SimState sim) {
        super.step(sim);
        setAngularVelocity(getAngularVelocity() * getCoefficientOfFriction());
    }

    public double getValue() {
        return value;
    }

}
