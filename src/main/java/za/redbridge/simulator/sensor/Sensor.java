package za.redbridge.simulator.sensor;

public abstract class Sensor {
    private final float position; //position on the agent as a bearing in degrees

    public Sensor(float position) {
        this.position = position;
    }

    public float getPosition() {
        return position;
    }
}
