package za.redbridge.simulator.object;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.physics2D.physicalObject.MobileObject2D;
import sim.physics2D.util.Angle;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;

import static za.redbridge.simulator.Utils.equal;

/**
 * Our implementation of {@link MobileObject2D}.
 * Created by jamie on 2014/07/25.
 */
public class MobileObject extends MobileObject2D implements Steppable, Taggable {

    private Double2D currentPosition = new Double2D();

    private String tag;

    public MobileObject() {
        super();
    }

    @Override
    public void setPose(Double2D position, Angle orientation) {
        super.setPose(position, orientation);
        currentPosition = position;
    }

    /**
     * An unfortunate part of the design of the Physics2D engine is that calls to
     * {@link #getPosition()} are not free - they check the object's position within the current
     * physics state and create a new Double2D instance. This gets the last calculated position
     * of the object.
     * @return the last updated position of this object
     */
    public Double2D getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void step(SimState simState) {
        Simulation simulation = (Simulation) simState;
        Double2D position = getPosition();
        if (!equal(position, currentPosition)) {
            simulation.getEnvironment().setObjectLocation(this, position);
            currentPosition = position;
        }
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
