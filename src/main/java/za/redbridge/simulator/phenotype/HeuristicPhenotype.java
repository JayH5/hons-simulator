package za.redbridge.simulator.phenotype;

import org.jbox2d.collision.Collision;
import org.jbox2d.common.Vec2;
import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.ResourceObject;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.PickupSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * Created by shsu on 2014/08/27.
 */
public class HeuristicPhenotype {

    private final CollisionSensor collisionSensor;
    private final PickupSensor pickupSensor;
    private final Phenotype controllerPhenotype;
    private final RobotObject attachedRobot;
    private final SimConfig.Direction targetAreaPlacement;

    protected static final double P2 = Math.PI / 2;

    //don't even know of this is necessary; just need next step
    private Stack<Vec2> pendingPath;

    public HeuristicPhenotype(Phenotype controllerPhenotype, RobotObject attachedRobot,
                              SimConfig.Direction targetAreaPlacement) {

        // TODO: Make configurable or decide on good defaults
        this.collisionSensor = new CollisionSensor();
        this.pickupSensor = new PickupSensor(1f, 2f, 0f);
        this.controllerPhenotype = controllerPhenotype;
        this.attachedRobot = attachedRobot;
        this.targetAreaPlacement = targetAreaPlacement;

        collisionSensor.attach(attachedRobot);
        pickupSensor.attach(attachedRobot);

        pendingPath = new Stack<>();
    }

    public CollisionSensor getCollisionSensor() { return collisionSensor; }
    public PickupSensor getPickupSensor() { return pickupSensor; }

    public HeuristicPhenotype clone() { return new HeuristicPhenotype(controllerPhenotype,
            attachedRobot, targetAreaPlacement); }

    public Double2D step(List<SensorReading> list) {

        Double2D wheelDrives;

        //if no pending path
        if (pendingPath.empty()) {
            Optional<Vec2> collision = collisionSensor.sense();
            wheelDrives = collision.map(o -> wheelDriveFromTargetPoint(o))
                    .orElse(controllerPhenotype.step(list));

            Optional<ResourceObject> sensedResource = pickupSensor.sense();

            /*Stack<Vec2> newPath = sensedResource.map(o -> getPerpendicularPathToPoint(attachedRobot.getBody().getLocalCenter(),
                    o.getBody().getLocalCenter())).orElse(new Stack<>());

            pendingPath = newPath;*/
        }
        //handle path overwrites as well here
        else {
            wheelDrives = wheelDriveFromTargetPoint(pendingPath.pop());

        }


        if (!attachedRobot.isBoundToResource()) {
            pickupSensor.sense().ifPresent(resource -> resource.tryPickup(attachedRobot));
        }
        else {
            wheelDrives = wheelDriveFromTargetPoint(guide(attachedRobot.getBody().getLocalCenter(),
                    attachedRobot.getBody().getLocalPoint(new Vec2(0, 0))));
        }

        //wheelDrives = wheelDriveFromTargetPosition(guide(attachedRobot.getBody().getLocalCenter(), attachedRobot.getBody().getLocalPoint(new Vec2(0,0))));

        return wheelDrives;

    }

    protected Double2D wheelDriveFromTargetPoint(Vec2 target){
        return wheelDriveFromBearing(bearingFromTargetPoint(target));
    }

    protected double bearingFromTargetPoint(Vec2 target){
        double angle = target.x != 0.0 ? Math.atan(target.y / target.x) : P2;
        if(target.x >= 0 && target.y >= 0){
            //first
        }else if(target.x < 0 && target.y >= 0){
            //second
            angle = P2 + (-angle);
        }else if(target.x <= 0 && target.y < 0){
            //third
            angle = 2* P2 + angle;
        }else if(target.x > 0 && target.y <= 0){
            //fourth
            angle = 3* P2 + (-angle);
        }else{
            throw new RuntimeException("bearingFromTargetPoint quadrant check failed!");
        }

        return angle;
    }

    protected Double2D wheelDriveFromBearing(double angle){
        double a, b;
        //4 quadrants
        if(angle <= P2 && angle >= 0.0){
            //first
            a = angle /P2;
            b = 1;
        }else if(angle <= 2* P2){
            //second
            a = (angle - P2) / P2;
            b = -1;
        }else if(angle <= 3* P2){
            //third
            a = -1;
            b = (angle - 2*P2) / P2;
        }else if(angle <= 4* P2){
            //fourth
            a = 1;
            b = (angle - 3*P2) / P2;
        }else{
            throw new RuntimeException("wheelDriveFromBearing quadrant check failed!");
        }
        return new Double2D(a, b);
    }

    //move one step so that you eventually get to the target area. (local, local)
    public Vec2 guide(Vec2 begin, Vec2 end) {

        int xDist = (int) Math.abs(end.x - begin.x);
        int yDist = (int) Math.abs(end.y - begin.y);

        int xDirectionMultiplier = (int)((end.x - begin.x)/xDist);
        int yDirectionMultiplier = (int)((end.x - begin.x)/yDist);

        Vec2 result = new Vec2 (begin.x+1*xDirectionMultiplier, begin.y+1*yDirectionMultiplier);
        return attachedRobot.getBody().getLocalPoint(result);

    }

    //returns wheeldrive heuristic steps for getting a point to a point with a perpendicular path. input should be global coords.
    public Stack<Vec2> getPerpendicularPathToPoint(Vec2 begin, Vec2 end) {

        //TODO: Convert to local coord system
        //size of graph to explore is a square with diag dist(begin, end)
        Stack<Vec2> path = new Stack<>();

        int xDist = (int) Math.abs(end.x - begin.x);
        int yDist = (int) Math.abs(end.y - begin.y);

        int xDirectionMultiplier = (int)((end.x - begin.x)/xDist);
        int yDirectionMultiplier = (int)((end.x - begin.x)/yDist);

        int i = 0;
        for (float x = begin.x; i < xDist; x+=xDirectionMultiplier*1) {

            path.push(attachedRobot.getBody().getLocalPoint(new Vec2(x, begin.y)));
            i++;
        }

        i = 0;
        for (float y = begin.y; i < yDist; y+=xDirectionMultiplier*1) {

            path.push(attachedRobot.getBody().getLocalPoint(new Vec2(begin.x+xDist*xDirectionMultiplier, y)));
            i++;
        }

        return path;
    }


}
