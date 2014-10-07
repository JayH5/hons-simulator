package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;
import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.Phenotype;

import java.awt.*;
import java.util.List;

public class HeterogeneousRobotFactory implements RobotFactory {
    protected float mass;
    protected float radius;
    protected Color color;
    protected List<Phenotype> phenotypes;

    public HeterogeneousRobotFactory(List<Phenotype> phenotypes, float mass, float radius, Color color) {
        this.phenotypes = phenotypes;
        this.mass = mass;
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world,
                               SimConfig.Direction targetAreaPlacement) {
        for (int i = 0; i < phenotypes.size(); i++) {
            PlacementArea.Space space = placementArea.getRandomCircularSpace(radius);

            Phenotype phenotype = phenotypes.get(i);

            RobotObject robot = new RobotObject(world, space.getPosition(), space.getAngle(),
                    radius, mass, color, phenotype, targetAreaPlacement);

            placementArea.placeObject(space, robot);
        }
    }
}
