package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;

import java.awt.Color;

import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.Phenotype;

public class HomogeneousRobotFactory implements RobotFactory {
    protected double mass;
    protected double radius;
    protected Color color;
    protected Phenotype phenotype;
    protected int numRobots;

    public HomogeneousRobotFactory(Phenotype phenotype, double mass, double radius, Color color, int numRobots) {
        this.phenotype = phenotype;
        this.mass = mass;
        this.radius = radius;
        this.color = color;
        this.numRobots = numRobots;
    }

    @Override
    public void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world,
                               SimConfig.Direction targetAreaPlacement) {
        for (int i = 0; i < numRobots; i++) {
            PlacementArea.Space space = placementArea.getRandomSpace(radius);

            Phenotype phenotype = this.phenotype.clone();

            RobotObject robot = new RobotObject(world, space.getPosition(), radius, mass, color,
                    phenotype, targetAreaPlacement);

            placementArea.placeObject(space, robot);
        }
    }

    public void setNumRobots(int numRobots) { this.numRobots = numRobots; }

    public int getNumRobots() { return numRobots; }
}
