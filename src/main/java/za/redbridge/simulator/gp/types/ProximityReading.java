package za.redbridge.simulator.gp.types;

import za.redbridge.simulator.sensor.AgentSensor;

import java.text.DecimalFormat;

/**
 * Created by xenos on 9/11/14.
 */
public class ProximityReading {

    protected float value;
    protected float bearing;
    protected float range;
    protected float fov;

    public ProximityReading(float value, AgentSensor sensor){
        this.value = value;
        this.bearing = sensor.getBearing();
        this.range = sensor.getRange();
        this.fov = sensor.getFieldOfView();
    }

    public float getValue() {
        return value;
    }

    public float getDistance(){
        return (1.0f - value) * range;
    }

    public Bearing getBearing(){
        return new Bearing(bearing);
    }

    /*
     * Note: this gets more and more terrible for higher fovs.
     */
    public RelativeCoordinate getCoordinate(){
        return RelativeCoordinate.fromDistAndBearing(getDistance(),getBearing());
    }

    public String toString(){
        DecimalFormat df = new DecimalFormat("#.00");
        return "ProximityReading " + df.format(value);
    }
}
