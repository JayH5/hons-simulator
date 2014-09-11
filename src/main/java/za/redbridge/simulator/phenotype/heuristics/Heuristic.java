package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.Vec2;

import java.util.List;

import sim.util.Double2D;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.SensorReading;

/**
 * Created by racter on 2014/09/01.
 */
public abstract class Heuristic implements Comparable<Heuristic> {

    public static final int SLOWDOWN_THRESHOLD = 1;
    protected static final double HALF_PI = Math.PI / 2;

    protected final RobotObject attachedRobot;
    private int priority;

    private final HeuristicSchedule schedule;

    public Heuristic(HeuristicSchedule schedule, RobotObject robot) {
        this.schedule = schedule;
        this.attachedRobot = robot;
    }

    protected HeuristicSchedule getSchedule() {
        return schedule;
    }

    protected void removeSelfFromSchedule() {
        schedule.removeHeuristic(this);
    }

    abstract Double2D step(List<SensorReading> list);

    /**
     * Calculate the drive force to get to the target point
     * @param target local to the agent
     * @return
     */
    protected static Double2D wheelDriveFromTargetPoint(Vec2 target){
        double dist = target.length();
        Double2D drive = wheelDriveForTargetPosition(target);
        if (dist < SLOWDOWN_THRESHOLD) {
            return drive.multiply(dist / SLOWDOWN_THRESHOLD);
        }else{
            return drive;
        }
    }

    /**
     * Get the wheel drive that will steer the agent towards the target position.
     * @param targetPosition The position of the target in local coordinates
     * @return the heuristic wheel drive
     */
    protected static Double2D wheelDriveForTargetPosition(Vec2 targetPosition) {
        return wheelDriveForTargetAngle(Math.atan2(targetPosition.y, targetPosition.x));
    }

    /**
     * Get the wheel drive that will steer the agent towards the target angle.
     * @param targetAngle The angle to the target
     * @return the heuristic wheel drive
     */
    protected static Double2D wheelDriveForTargetAngle(double targetAngle) {
        final double left, right;
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

    public int getPriority() { return priority; }

    public void setPriority(int priority) { this.priority = priority; }

    public RobotObject getAttachedRobot() { return attachedRobot; }

    public double distance (Vec2 start, Vec2 end) {

        double xDiff = (end.x - start.x);
        double yDiff = (end.y - start.y);

        return Math.sqrt((xDiff*xDiff) - (yDiff*yDiff));
    }

    @Override
    public int compareTo(Heuristic other) {
        return Integer.compare(getPriority(), other.getPriority());
    }

}
