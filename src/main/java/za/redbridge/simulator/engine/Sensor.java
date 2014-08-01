package za.redbridge.simulator.engine;

import sim.util.Double2D;

public abstract class Sensor {
    private float position; //position on the agent as a bearing in degrees

    public Sensor(float position) {
        this.position = position;
    }

    public float getPosition() {
        return position;
    }
}
