package za.redbridge.simulator.phenotype.heuristics;

import java.util.List;

import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.PickupSensor;
import za.redbridge.simulator.sensor.SensorReading;

/**
 * Heuristic for picking up things and carrying them to target area
 * Created by racter on 2014/09/01.
 */
public class PickupHeuristic extends Heuristic {

    protected final PickupSensor pickupSensor;
    protected final SimConfig.Direction targetAreaDirection;

    public PickupHeuristic(HeuristicSchedule schedule, PickupSensor pickupSensor,
            RobotObject attachedRobot, SimConfig.Direction targetAreaDirection) {
        super(schedule, attachedRobot);
        this.pickupSensor = pickupSensor;
        this.targetAreaDirection = targetAreaDirection;

        setPriority(3);
    }

    @Override
    public Double2D step(List<SensorReading> list) {
        // Go for the target area if we've managed to attach to a resource
        if (attachedRobot.isBoundToResource()) {
            return wheelDriveForTargetAngle(targetAreaAngle());
        }

        // Check for a resource in the sensor
        ResourceObject resource =
                pickupSensor.sense().map(o -> (ResourceObject) o.getObject()).orElse(null);
        if (resource == null || !resource.canBePickedUp()) {
            return null; // No viable resource, nothing to do
        }

        // Try pick it up
        if (resource.tryPickup(attachedRobot)) {
            // Success! Head for the target zone
            return wheelDriveForTargetAngle(targetAreaAngle());
        } else {
            // Couldn't pick it up, add a heuristic to navigate to the resource
            getSchedule().addHeuristic(
                    new PickupPositioningHeuristic(getSchedule(), pickupSensor, attachedRobot));
        }

        return null;
    }

    //target area bearing from robot angle
    protected double targetAreaAngle() {
        double robotAngle = attachedRobot.getBody().getTransform().q.getAngle();
        double targetAreaPosition = -1;

        if (targetAreaDirection == SimConfig.Direction.NORTH) {
            targetAreaPosition = HALF_PI;
        } else if (targetAreaDirection == SimConfig.Direction.SOUTH) {
            targetAreaPosition = -HALF_PI;
        } else if (targetAreaDirection == SimConfig.Direction.EAST) {
            targetAreaPosition = 0;
        } else if (targetAreaDirection == SimConfig.Direction.WEST) {
            targetAreaPosition = Math.PI;
        }

        double difference = targetAreaPosition - robotAngle;
        return difference % (Math.PI * 2);

    }

}
