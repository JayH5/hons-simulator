package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.Vec2;

import java.awt.Color;
import java.util.List;

import sim.util.Double2D;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.ClosestObjectSensor;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.Sensor;


import static za.redbridge.simulator.Utils.jitter;

/**
 * Created by racter on 2014/09/01.
 */
public class CollisionAvoidanceHeuristic extends Heuristic {

    private static final Color COLOR = Color.RED;

    protected final CollisionSensor collisionSensor;

    public CollisionAvoidanceHeuristic(CollisionSensor collisionSensor, RobotObject robot) {
        super(robot);
        this.collisionSensor = collisionSensor;

        setPriority(4);
    }

    @Override
    public Double2D step(List<List<Double>> list) {
        ClosestObjectSensor.ClosestObject collision = collisionSensor.sense();
        if (collision != null) {
            Vec2 position = collision.getVectorToObject().negate();
            return wheelDriveForTargetPosition(jitter(position, 0.2f));
        }
        return null;
    }

    @Override
    Color getColor() {
        return COLOR;
    }

    @Override
    public Sensor getSensor() {
        return collisionSensor;
    }

}
