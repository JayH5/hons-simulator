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

        Vec2 newPosition = nextStep();

        Optional<ResourceObject> sensedResource = pickupSensor.sense();

        if (!attachedRobot.isBoundToResource()) {

            Vec2 attachmentResult = sensedResource.map(resource -> resource.tryPickup(attachedRobot))
                    .orElse(new Vec2(-4, 0));

            if (attachmentResult.y < 0) {
                System.out.println("Success!");
                schedule.remove(this);
            }
            else if (resource.getBody().getPosition().sub(attachedRobot.getBody().getPosition()).length() > resource.getHypot()*2) {

                schedule.remove(this);
            }
        }

        //System.out.println("Wheeldrives " + wheelDriveFromTargetPoint(attachedRobot.getBody().getLocalPoint(newPosition)).x + "," + wheelDriveFromTargetPoint(attachedRobot.getBody().getLocalPoint(newPosition)).y);

        return wheelDriveFromTargetPoint(attachedRobot.getBody().getLocalPoint(newPosition));

    }

    //next step (out: world)
    private Vec2 nextStep() {

        ResourceObject.Side stickySide = resource.getStickySide();
        ResourceObject.Side robotSide = resource.getSideClosestToPoint(attachedRobot.getBody().getPosition());

        //System.out.println("Pathing from " + robotSide.name() + " to " + stickySide.name() + "...");

        Vec2 closestAttachmentPoint = resource.getClosestAnchorPointWorld(attachedRobot.getBody().getPosition());
        Vec2 robotPosition = attachedRobot.getBody().getPosition();

        if (stickySide == null) {
            return new Vec2(-8, 0);
        }

        Vec2 position;
        double width = resource.getWidth();
        double height = resource.getHeight();

        //same side
        if (stickySide == robotSide) {
            return straightGuide(robotPosition, closestAttachmentPoint);
        }
        //different side
        else {

            System.out.println("whoa diffside");

            Vec2 robotPositionLocalToResource = resource.getBody().getLocalPoint(robotPosition);

            float spacing = 0.0f;

            double xDist = Math.abs(closestAttachmentPoint.x - robotPosition.x);
            double yDist = Math.abs(closestAttachmentPoint.y - robotPosition.y);

            int xDirectionMultiplier = (int)((closestAttachmentPoint.x - robotPosition.x)/xDist)*-1;
            int yDirectionMultiplier = (int)((closestAttachmentPoint.y - robotPosition.y)/yDist)*-1;

            //anchor point relative to the ResourceObject
            if (robotSide == ResourceObject.Side.LEFT || robotSide == ResourceObject.Side.RIGHT) {

                float y = (float) height / 2 - yDirectionMultiplier*(robotPositionLocalToResource.y + spacing / 2);
                float x = robotSide == ResourceObject.Side.LEFT ? (float) -width / 2 : (float) width / 2;
                position = new Vec2(x,y);
            } else {

                float x = (float) -width / 2 + xDirectionMultiplier*(robotPositionLocalToResource.x + spacing / 2);
                float y = robotSide == ResourceObject.Side.TOP ? (float) -height / 2 : (float) height / 2;
                position = new Vec2(x,y);
            }

            return resource.getBody().getWorldPoint(position);

        }

    }

    //next step in straight line along axis of greatest change
    public Vec2 straightGuide(Vec2 begin, Vec2 end) {

        double xDist = Math.abs(end.x - begin.x);
        double yDist = Math.abs(end.y - begin.y);

        if (xDist < 0.01 || yDist < 0.01) {
            return begin;
        }

        int xDirectionMultiplier = (int)((end.x - begin.x)/xDist);
        int yDirectionMultiplier = (int)((end.y - begin.y)/yDist);

        Vec2 result;

        if (xDist > yDist) {
            result = new Vec2 (begin.x+xDirectionMultiplier, begin.y);
        }
        else {
            result = new Vec2 (begin.x, begin.y+yDirectionMultiplier);
        }

        return result;
    }

}
