package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.Vec2;

import java.util.List;
import java.util.Optional;

import sim.util.Double2D;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.ClosestObjectSensor;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.SensorReading;

/**
 * Created by racter on 2014/09/01.
 */
public class CollisionAvoidanceHeuristic extends Heuristic {

    protected final CollisionSensor collisionSensor;

    public CollisionAvoidanceHeuristic(HeuristicSchedule schedule, CollisionSensor collisionSensor,
            RobotObject attachedRobot) {
        super(schedule, attachedRobot);
        this.collisionSensor = collisionSensor;

        setPriority(4);
    }

    @Override
    public Double2D step(List<SensorReading> list) {
        Optional<ClosestObjectSensor.ClosestObject> collision = collisionSensor.sense();

        Double2D wheelDrives = collision.map(o -> o.getVectorToObject())
                .map(o -> wheelDriveFromTargetPoint(o.negate()))
                .orElse(null);

        return wheelDrives;
    }

}
