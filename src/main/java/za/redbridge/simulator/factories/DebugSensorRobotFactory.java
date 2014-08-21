package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;

import java.awt.Paint;

import sim.util.Double2D;
import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.Phenotype;

public class DebugSensorRobotFactory implements RobotFactory {
    protected double mass;
    protected double radius;
    protected Paint paint;
    protected Phenotype phenotype;

    public DebugSensorRobotFactory(Phenotype phenotype, double mass, double radius, Paint paint) {
        this.phenotype = phenotype;
        this.mass = mass;
        this.radius = radius;
        this.paint = paint;
    }

    @Override
    public void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world,
            int quantity) {
        PlacementArea.Space space =
                placementArea.getSpaceAtPosition(radius, new Double2D(50, 50));
        RobotObject r1 =
                new RobotObject(world, space.getPosition(), radius, mass, paint, phenotype.clone());
        placementArea.placeObject(space, r1);

        space = placementArea.getSpaceAtPosition(radius, new Double2D(70, 50));
        RobotObject r2 =
                new RobotObject(world, space.getPosition(), radius, mass, paint, phenotype.clone());
        placementArea.placeObject(space, r2);
    }
}
