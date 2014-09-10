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
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Heuristic for picking up things and carrying them to target area
 * Created by racter on 2014/09/01.
 */
public class PickupHeuristic extends Heuristic {

    protected final PickupSensor pickupSensor;
    protected final RobotObject attachedRobot;
    protected final SimConfig.Direction targetAreaBearing;
    protected final PriorityBlockingQueue<Heuristic> schedule;

    protected int priority = 3;

    public PickupHeuristic(PickupSensor pickupSensor, RobotObject attachedRobot,
                           PriorityBlockingQueue<Heuristic> schedule,
                           SimConfig.Direction targetAreaBearing) {

        this.pickupSensor = pickupSensor;
        this.attachedRobot = attachedRobot;
        this.targetAreaBearing = targetAreaBearing;
        this.schedule = schedule;
    }

    @Override
    public Double2D step(List<SensorReading> list) {
        Double2D wheelDrives = null;
        Optional<ResourceObject> sensedResource = pickupSensor.sense();

        /*
        if (sensedResource.isPresent() && !attachedRobot.isBoundToResource())
            System.out.println("sensed resource");*/

        if (!sensedResource.isPresent()) {

            return wheelDrives;
        }

        if (!attachedRobot.isBoundToResource()) {

            boolean attachmentSuccess = sensedResource.map(resource -> resource.tryPickup(attachedRobot))
                    .orElse(false);

            if (attachmentSuccess) {
                wheelDrives = wheelDriveFromBearing(targetAreaBearing());
            }
            else if (sensedResource.isPresent()) {
                ResourceObject resource = sensedResource.get();

                if (resource.pushedByMaxRobots() || resource.isCollected()) {
                    return null;
                }
                else {

                    schedule.add(new PickupPositioningHeuristic(sensedResource.get(), pickupSensor,
                            attachedRobot, schedule));
                }

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
