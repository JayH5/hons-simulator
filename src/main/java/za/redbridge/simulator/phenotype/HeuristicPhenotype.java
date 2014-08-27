package za.redbridge.simulator.phenotype;

import org.jbox2d.collision.Collision;
import org.jbox2d.common.Vec2;
import sim.util.Double2D;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.PickupSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by shsu on 2014/08/27.
 */
public class HeuristicPhenotype {

    private CollisionSensor collisionSensor;
    private PickupSensor pickupSensor;
    private Phenotype controllerPhenotype;

    public HeuristicPhenotype(CollisionSensor collisionSensor, PickupSensor pickupSensor, Phenotype controllerPhenotype) {

        this.collisionSensor = new CollisionSensor();
        this.pickupSensor = new PickupSensor(1f, 2f, 0f);
        this.controllerPhenotype = controllerPhenotype;
    }

    public CollisionSensor getCollisionSensor() { return collisionSensor; }
    public PickupSensor getPickupSensor() { return pickupSensor; }

    public HeuristicPhenotype clone() { return new HeuristicPhenotype(collisionSensor, pickupSensor, controllerPhenotype); }

    public Double2D step(List<SensorReading> list) {

        Optional<Vec2> collision = collisionSensor.sense();
        Double2D wheelDrives = collision.map(o -> wheelDriveFromTargetPosition(o))
                .orElse(controllerPhenotype.step(list));

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

}
