package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.Vec2;
import sim.util.Double2D;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Created by racter on 2014/09/01.
 */
public class CollisionAvoidanceHeuristic extends Heuristic {

    protected final CollisionSensor collisionSensor;
    protected final RobotObject attachedRobot;
    protected int priority = 5;

    public CollisionAvoidanceHeuristic(CollisionSensor collisionSensor, RobotObject attachedRobot) {

        this.collisionSensor = collisionSensor;
        this.attachedRobot = attachedRobot;
    }

    @Override
    public Double2D step(List<SensorReading> list) {

        Double2D wheelDrives = null;
        Optional<Vec2> collision = collisionSensor.sense();

        wheelDrives = collision.map(o -> wheelDriveFromTargetPoint(o))
                .orElse(null);

        return wheelDrives;
    }

}
