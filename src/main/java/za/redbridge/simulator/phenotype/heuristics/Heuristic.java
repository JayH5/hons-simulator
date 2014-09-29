package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;

import sim.util.Double2D;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.Sensor;

/**
 * Created by racter on 2014/09/01.
 */
public abstract class Heuristic implements Comparable<Heuristic> {

    protected static final double HALF_PI = Math.PI / 2;

    protected final RobotObject robot;
    private int priority;

    private HeuristicSchedule schedule;

    public Heuristic(RobotObject robot) {
        this.robot = robot;
    }

    protected HeuristicSchedule getSchedule() {
        return schedule;
    }

    protected void removeSelfFromSchedule() {
        schedule.removeHeuristic(this);
    }

    abstract Double2D step(List<List<Double>> list);

    /**
     * Color used to change colour of robot as each heuristic takes over.
     * @return the color to colour the agent when the heuristic returns a value
     */
    abstract Color getColor();

    /**
     * Get the sensor for this heuristic. May return null if no sensor is used.
     */
    public abstract Sensor getSensor();

    /**
     * Get the wheel drive that will steer the agent towards the target position.
     * @param targetPosition The position of the target in local coordinates
     * @return the heuristic wheel drive
     */
    protected static Double2D wheelDriveForTargetPosition(Vec2 targetPosition) {
        return wheelDriveForTargetAngle(MathUtils.atan2(targetPosition.y, targetPosition.x));
    }

    /**
     * Get the wheel drive that will steer the agent towards the target angle.
     * @param targetAngle The angle to the target
     * @return the heuristic wheel drive
     */
    protected static Double2D wheelDriveForTargetAngle(double targetAngle) {
        final double left, right;
        if(Math.abs(targetAngle) > Math.PI) targetAngle = Math.PI*Math.signum(targetAngle);
        // Different response for each of four quadrants
        if (targetAngle >= 0) {
            if (targetAngle < HALF_PI) {
                // First
                left = (HALF_PI - targetAngle) / HALF_PI;
                right = 1;
            } else {
                // Second
                left = -(targetAngle - HALF_PI) / HALF_PI;
                right = -1;
            }
        } else {
            if (targetAngle < -HALF_PI) {
                // Third
                left = -1;
                right = (targetAngle + HALF_PI) / HALF_PI;
            } else {
                // Fourth
                left = 1;
                right = (HALF_PI + targetAngle) / HALF_PI;
            }
        }

        return new Double2D(left, right);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /* package */ RobotObject getRobot() {
        return robot;
    }

    /* package */ void setSchedule(HeuristicSchedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public int compareTo(Heuristic other) {
        return Integer.compare(getPriority(), other.getPriority());
    }

}
