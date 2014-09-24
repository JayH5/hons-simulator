package za.redbridge.simulator.phenotype.heuristics;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.Optional;

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

    private static final Paint PAINT = Color.RED;

    protected final CollisionSensor collisionSensor;

    public CollisionAvoidanceHeuristic(CollisionSensor collisionSensor, RobotObject robot) {
        super(robot);
        this.collisionSensor = collisionSensor;

        setPriority(4);
    }

    @Override
    public Double2D step(List<List<Double>> list) {
        Optional<ClosestObjectSensor.ClosestObject> collision = collisionSensor.sense();

        return collision.map(o -> o.getVectorToObject())
                .map(o -> wheelDriveForTargetPosition(jitter(o.negate(), 0.2f)))
                .orElse(null);
    }

    @Override
    Paint getPaint() {
        return PAINT;
    }

    @Override
    public Sensor getSensor() {
        return collisionSensor;
    }

}
