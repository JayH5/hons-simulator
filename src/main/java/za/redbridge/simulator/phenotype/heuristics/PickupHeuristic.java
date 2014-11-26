package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.Vec2;

import java.awt.Color;
import java.util.List;

import sim.util.Double2D;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.ClosestObjectSensor;
import za.redbridge.simulator.sensor.PickupSensor;
import za.redbridge.simulator.sensor.Sensor;

/**
 * Heuristic for picking up things and carrying them to target area
 * Created by racter on 2014/09/01.
 */
public class PickupHeuristic extends Heuristic {

    private static final Color COLOR = Color.GREEN;
    private static final boolean ENABLE_PICKUP_POSITIONING = false;

    protected final PickupSensor pickupSensor;
    protected final Vec2 targetAreaPosition;

    public PickupHeuristic(PickupSensor pickupSensor, RobotObject robot, Vec2 targetAreaPosition) {
        super(robot);
        this.pickupSensor = pickupSensor;
        this.targetAreaPosition = targetAreaPosition;

        setPriority(3);
    }

    @Override
    public Double2D step(List<List<Double>> list) {
        // Go for the target area if we've managed to attach to a resource
        if (robot.isBoundToResource()) {
            return wheelDriveForTargetPosition(robot.getBody().getLocalPoint(targetAreaPosition));
        }

        // Check for a resource in the sensor
        ClosestObjectSensor.ClosestObject closestObject = pickupSensor.sense();
        ResourceObject resource = closestObject != null ?
                (ResourceObject) closestObject.getObject() : null;
        if (resource == null || !resource.canBePickedUp()) {
            return null; // No viable resource, nothing to do
        }

        // Try pick it up
        if (resource.tryPickup(robot)) {
            // Success! Head for the target zone
            return wheelDriveForTargetPosition(robot.getBody().getLocalPoint(targetAreaPosition));
        } else if (ENABLE_PICKUP_POSITIONING) {
            // Couldn't pick it up, add a heuristic to navigate to the resource
            getSchedule().addHeuristic(new PickupPositioningHeuristic(pickupSensor, robot));
        }

        return null;
    }

    @Override
    Color getColor() {
        return COLOR;
    }

    @Override
    public Sensor getSensor() {
        return pickupSensor;
    }

}
