package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;

import java.awt.Paint;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.TargetAreaObject;
import za.redbridge.simulator.phenotype.HeuristicPhenotype;
import za.redbridge.simulator.phenotype.Phenotype;

public class HomogeneousRobotFactory implements RobotFactory {
    protected double mass;
    protected double radius;
    protected Paint paint;
    protected Phenotype phenotype;

    public HomogeneousRobotFactory(Phenotype phenotype, double mass, double radius, Paint paint) {
        this.phenotype = phenotype;
        this.mass = mass;
        this.radius = radius;
        this.paint = paint;
    }

    @Override
    public void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world,
            int quantity, SimConfig.Direction targetAreaPlacement) {
        for (int i = 0; i < quantity; i++) {
            PlacementArea.Space space = placementArea.getRandomSpace(radius);

            Phenotype phenotype = this.phenotype.clone();

            RobotObject robot =
                    new RobotObject(world, space.getPosition(), radius, mass, paint, phenotype, targetAreaPlacement);

            placementArea.placeObject(space, robot);
        }
    }
}
