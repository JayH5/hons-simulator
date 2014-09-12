package za.redbridge.simulator.phenotype.heuristics;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.Optional;

import sim.util.Double2D;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.ClosestObjectSensor;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.SensorReading;


import static za.redbridge.simulator.Utils.jitter;

/**
 * Created by racter on 2014/09/01.
 */
public class CollisionAvoidanceHeuristic extends Heuristic {

    private static final Paint PAINT = Color.RED;

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
                .map(o -> wheelDriveForTargetPosition(jitter(o.negate(), 0.2f)))
                .orElse(null);

        return wheelDrives;
    }

    @Override
    Paint getPaint() {
        return PAINT;
    }

}
