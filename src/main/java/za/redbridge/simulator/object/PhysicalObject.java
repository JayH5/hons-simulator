package za.redbridge.simulator.object;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.physics2D.physicalObject.MobileObject2D;
import sim.physics2D.util.Angle;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;

/**
 * Created by jamie on 2014/07/25.
 */
public class PhysicalObject extends MobileObject2D implements Steppable {

    private static final double EPSILON = 0.0001;

    private Double2D previousPosition = new Double2D();

    public PhysicalObject() {
        super();
    }

    @Override
    public void setPose(Double2D position, Angle orientation) {
        previousPosition = getPosition();
        super.setPose(position, orientation);
    }

    public boolean positionChanged(Double2D position) {
        return Math.abs(position.x - previousPosition.x) >= EPSILON
                || Math.abs(position.y - previousPosition.y) >= EPSILON;
    }


    @Override
    public void step(SimState simState) {
        Simulation simulation = (Simulation) simState;
        Double2D position = getPosition();
        if (positionChanged(position)) {
            simulation.getEnvironment().setObjectLocation(this, position);
        }
    }
}
