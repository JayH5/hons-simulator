package za.redbridge.simulator.object;

import sim.physics2D.physicalObject.StationaryObject2D;

/**
 * Our implementation of {@link StationaryObject2D}.
 * Created by jamie on 2014/08/03.
 */
public class StationaryObject extends StationaryObject2D implements Taggable {

    private String tag;

    public StationaryObject() {
        super();
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
