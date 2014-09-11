package za.redbridge.simulator.gp.types;

import org.jbox2d.common.Vec2;
import sim.util.Double2D;
import za.redbridge.simulator.phenotype.heuristics.Heuristic;

/**
 * Created by xenos on 9/11/14.
 */
public class WheelDrive extends Vec2 {
    public WheelDrive(float x, float y){
        super(clamp(x), clamp(y));
    }

    public WheelDrive(Bearing bearing){
        super((float) Heuristic.wheelDriveForTargetAngle(bearing.getValue()).x, (float) Heuristic.wheelDriveForTargetAngle(bearing.getValue()).y);
    }

    public WheelDrive(RelativeCoordinate c){
        super((float)Heuristic.wheelDriveFromTargetPoint(c).x, (float)Heuristic.wheelDriveFromTargetPoint(c).y);
    }

    private static float clamp(float f){
        return (float)Math.max(0.0, Math.min(1.0, f));
    }
}
