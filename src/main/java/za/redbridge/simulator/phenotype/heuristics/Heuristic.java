package za.redbridge.simulator.phenotype.heuristics;

import org.jbox2d.common.Vec2;
import sim.util.Double2D;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.List;
import java.util.Comparator;

/**
 * Created by racter on 2014/09/01.
 */
public abstract class Heuristic implements Comparator<Heuristic>{

    protected static final double P2 = Math.PI / 2;

    protected RobotObject attachedRobot;
    protected int priority;

    public abstract Double2D step (List<SensorReading> list);

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Heuristic)) {
            throw new ClassCastException("Supplied object not member of Heuristic class.");
        }
        else
            return this.getPriority() == ((Heuristic) o).getPriority();
    }

    protected Double2D wheelDriveFromTargetPoint(Vec2 target){
        return wheelDriveFromBearing(bearingFromTargetPoint(target));
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
            throw new RuntimeException("wheelDriveFromBearing quadrant check failed! " + angle);
        }
        return new Double2D(a, b);
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

    public int getPriority() { return priority; }

    public void setPriority(int priority) { this.priority = priority; }

    public RobotObject getAttachedRobot() { return attachedRobot; }

    public double distance (Vec2 start, Vec2 end) {

        double xDiff = (end.x - start.x);
        double yDiff = (end.y - start.y);

        return Math.sqrt((xDiff*xDiff) - (yDiff*yDiff));
    }

    @Override
    public int compare(Heuristic a, Heuristic b) {

        return a.getPriority() - b.getPriority();
    }

}
