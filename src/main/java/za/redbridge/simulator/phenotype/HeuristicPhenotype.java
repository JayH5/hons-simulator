package za.redbridge.simulator.phenotype;

import org.jbox2d.collision.Collision;
import org.jbox2d.common.Vec2;
import sim.util.Double2D;
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

    private Stack<Vec2> pendingPath;

    public HeuristicPhenotype(Phenotype controllerPhenotype, RobotObject attachedRobot) {

        // TODO: Make configurable or decide on good defaults
        this.collisionSensor = new CollisionSensor();
        this.pickupSensor = new PickupSensor(1f, 2f, 0f);
        this.controllerPhenotype = controllerPhenotype;
        this.attachedRobot = attachedRobot;

        collisionSensor.attach(attachedRobot);
        pickupSensor.attach(attachedRobot);

        pendingPath = new Stack<>();
    }

    public CollisionSensor getCollisionSensor() { return collisionSensor; }
    public PickupSensor getPickupSensor() { return pickupSensor; }

    public HeuristicPhenotype clone() { return new HeuristicPhenotype(controllerPhenotype, attachedRobot); }

    public Double2D step(List<SensorReading> list) {

        Double2D wheelDrives;

        //if no pending path
        if (pendingPath.empty()) {
            Optional<Vec2> collision = collisionSensor.sense();
            wheelDrives = collision.map(o -> wheelDriveFromTargetPosition(o))
                    .orElse(controllerPhenotype.step(list));

            if (!attachedRobot.isBoundToResource()) {
                pickupSensor.sense().ifPresent(resource -> resource.tryPickup(attachedRobot));
            }

        }
        //handle path overwrites as well here
        else {
            wheelDrives = wheelDriveFromTargetPosition(pendingPath.pop());
        }

        return wheelDrives;
    }

    protected Double2D wheelDriveFromTargetPosition(Vec2 targetPos){
        double p2 = Math.PI / 2;

        //handle division by 0
        double angle = targetPos.x != 0.0 ? Math.atan(targetPos.y / targetPos.x) : p2;

        double a, b;
        //4 quadrants
        if(targetPos.x >= 0 && targetPos.y > 0){
            //first
            a = (p2 - angle) / p2;
            b = 1;
        }else if(targetPos.x < 0 && targetPos.y >= 0){
            //second
            a = -((p2 + angle) / p2);
            b = -1;
        }else if(targetPos.x <= 0 && targetPos.y < 0){
            //third
            a = -1;
            b = -((p2 - angle) / p2);
        }else if(targetPos.x > 0 && targetPos.y <= 0){
            //fourth
            a = 1;
            b = (p2 + angle) / p2;
        }else{
            throw new RuntimeException("wheelDriveFromTargetPosition quadrant check failed!");
        }
        return new Double2D(a, b);
    }

    //returns wheeldrive heuristic steps for getting a point to a point with a perpendicular path. input should be global coords.
    public Stack<Vec2> getPerpendicularPathToPoint(Vec2 begin, Vec2 end) {

        //size of graph to explore is a square with diag dist(begin, end)
        Stack<Vec2> path = new Stack<>();

        int xDist = (int) Math.abs(end.x - begin.x);
        int yDist = (int) Math.abs(end.y - begin.y);

        int xDirectionMultiplier = (int)((end.x - begin.x)/xDist);
        int yDirectionMultiplier = (int)((end.x - begin.x)/yDist);

        int i = 0;
        for (float x = begin.x; i < xDist; x+=xDirectionMultiplier*1) {

            path.push(new Vec2(x, begin.y));
            i++;
        }

        i = 0;
        for (float y = begin.y; i < yDist; y+=xDirectionMultiplier*1) {

            path.push(new Vec2(begin.x+xDist*xDirectionMultiplier, y));
            i++;
        }

        return path;
    }


}
