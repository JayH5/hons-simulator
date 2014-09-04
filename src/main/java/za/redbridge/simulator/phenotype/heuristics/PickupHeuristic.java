package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.Vec2;
import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.PickupSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.List;
import java.util.Optional;

/**
 * Heuristic for picking up things and carrying them to target area
 * Created by racter on 2014/09/01.
 */
public class PickupHeuristic extends Heuristic {

    protected final PickupSensor pickupSensor;
    protected final RobotObject attachedRobot;
    protected final SimConfig.Direction targetAreaBearing;
    protected int priority = 6;

    public PickupHeuristic(PickupSensor pickupSensor, RobotObject attachedRobot,
                           SimConfig.Direction targetAreaBearing) {

        this.pickupSensor = pickupSensor;
        this.attachedRobot = attachedRobot;
        this.targetAreaBearing = targetAreaBearing;
    }

    @Override
    public Double2D step(List<SensorReading> list) {

        Double2D wheelDrives = null;
        Optional<ResourceObject> sensedResource = pickupSensor.sense();

        if (!attachedRobot.isBoundToResource()) {

            boolean success = sensedResource.map(resource -> resource.tryPickup(attachedRobot))
                    .orElse(false);

            if (success) {
                wheelDrives = wheelDriveFromBearing(targetAreaBearing());
            }
        }
        else {
            wheelDrives = wheelDriveFromBearing(targetAreaBearing());
        }

        return wheelDrives;
    }

    //target area bearing from robot angle
    protected double targetAreaBearing() {

        double robotAngle = (attachedRobot.getBody().getTransform().q.getAngle()+(4*P2))%(4*P2);
        double targetAreaPosition = -1;

        if (targetAreaBearing == SimConfig.Direction.NORTH) {
            targetAreaPosition = P2*3;
        }
        else if (targetAreaBearing == SimConfig.Direction.SOUTH) {
            targetAreaPosition = P2;
        }
        else if (targetAreaBearing == SimConfig.Direction.EAST) {
            targetAreaPosition = 0;
        }
        else if (targetAreaBearing == SimConfig.Direction.WEST) {
            targetAreaPosition = P2*2;
        }

        double difference = targetAreaPosition - robotAngle;
        double bearing = (4*P2 + difference)%(4*P2);

        return bearing;

    }

}
