package za.redbridge.simulator.object;

import sim.engine.SimState;
import sim.engine.Steppable;
import za.redbridge.simulator.portrayal.RectanglePortrayal2D;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject implements Steppable {

    private final RectanglePortrayal2D portrayal;

    private final double value;
    private final double weight;

    public ResourceObject(double width, double height, double value, double weight) {
        portrayal = new RectanglePortrayal2D(width, height);
        this.value = value;
        this.weight = weight;
    }

    public double getValue() {
        return value;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public void step(SimState state) {
        // TODO: Do something?
    }
}
