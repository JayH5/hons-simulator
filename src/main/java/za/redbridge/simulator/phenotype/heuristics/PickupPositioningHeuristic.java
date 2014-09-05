package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.Vec2;
import sim.util.Double2D;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.PickupSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by shsu on 2014/09/04.
 */
public class PickupPositioningHeuristic extends Heuristic {

    protected final ResourceObject resource;
    protected final PickupSensor pickupSensor;
    protected final RobotObject attachedRobot;
    protected final PriorityBlockingQueue<Heuristic> schedule;

    protected int priority = 2;

    public PickupPositioningHeuristic (ResourceObject resource, PickupSensor pickupSensor,
                                       RobotObject attachedRobot,
                                       PriorityBlockingQueue<Heuristic> schedule) {

        this.resource = resource;
        this.attachedRobot = attachedRobot;
        this.schedule = schedule;
        this.pickupSensor = pickupSensor;
    }

    public Double2D step(List<SensorReading> list) {

        Double2D wheelDrives = null;

        Vec2 newPosition = guide(attachedRobot.getBody().getWorldCenter(),
                resource.getBody().getWorldCenter());

        Optional<ResourceObject> sensedResource = pickupSensor.sense();

        if (!attachedRobot.isBoundToResource()) {

            boolean success = sensedResource.map(resource -> resource.tryPickup(attachedRobot))
                    .orElse(false);

            if (success) {
                System.out.println("Success!");
                schedule.remove(this);
            }
            else if (resource.getBody().getWorldCenter().sub(attachedRobot.getBody().getWorldCenter()).length() > attachedRobot.getRadius()*3) {
                System.out.println("I'm too fucking far.");
                schedule.remove(this);
            }
        }

        System.out.println("Wheeldrives " + wheelDriveFromTargetPoint(attachedRobot.getBody().getLocalPoint(newPosition)).x + "," + wheelDriveFromTargetPoint(attachedRobot.getBody().getLocalPoint(newPosition)).y);

        return wheelDriveFromTargetPoint(attachedRobot.getBody().getLocalPoint(newPosition));

    }

    //move one step so that you eventually get to the target area. (world, world)
    public Vec2 guide(Vec2 begin, Vec2 end) {

        System.out.println("Begin x: " + begin.x + " Begin y: " + begin.y);
        System.out.println("End x: " + end.x + " End y: " + end.y);

        System.out.println("Dist is " + Math.sqrt(end.sub(begin).lengthSquared()));


        double xDist = Math.abs(end.x - begin.x);
        double yDist = Math.abs(end.y - begin.y);

        System.out.println("xdist " + xDist + " yDist " + yDist);

        if (xDist < 0.01 || yDist < 0.01) {
            return begin;
        }

        int xDirectionMultiplier = (int)((end.x - begin.x)/xDist);
        int yDirectionMultiplier = (int)((end.y - begin.y)/yDist);

        System.out.println("Pickup sensor x: " + pickupSensor.getBody().getWorldCenter().x + " y: " + pickupSensor.getBody().getWorldCenter().y + " xMultiplier: " + xDirectionMultiplier + " yMultiplier: " + yDirectionMultiplier);

        Vec2 result = new Vec2 (begin.x+xDirectionMultiplier, begin.y+yDirectionMultiplier);
        return result;

    }
}
