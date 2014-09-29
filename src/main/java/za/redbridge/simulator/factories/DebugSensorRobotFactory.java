package za.redbridge.simulator.factories;

import org.jbox2d.dynamics.World;

import java.awt.Color;

import sim.util.Double2D;
import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.HeuristicPhenotype;
import za.redbridge.simulator.phenotype.Phenotype;

public class DebugSensorRobotFactory implements RobotFactory {
    protected double mass;
    protected double radius;
    protected Color color;
    protected Phenotype phenotype;
    protected HeuristicPhenotype heuristicPhenotype;
    protected int numRobots;

    public DebugSensorRobotFactory(Phenotype phenotype,
                                   double mass, double radius, Color color, int numRobots) {
        this.phenotype = phenotype;
        this.mass = mass;
        this.radius = radius;
        this.color = color;
        this.numRobots = numRobots;
    }

    @Override
    public void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world,
                               SimConfig.Direction targetAreaPlacement) {
        PlacementArea.Space space =
                placementArea.getSpaceAtPosition(radius, new Double2D(50, 50));
        RobotObject r1 = new RobotObject(world, space.getPosition(), radius, mass, color,
                phenotype.clone(), targetAreaPlacement);
        placementArea.placeObject(space, r1);

        space = placementArea.getSpaceAtPosition(radius, new Double2D(70, 50));
        RobotObject r2 = new RobotObject(world, space.getPosition(), radius, mass, color,
                phenotype.clone(), targetAreaPlacement);
        placementArea.placeObject(space, r2);
    }

    public void setNumRobots(int numRobots) { this.numRobots = numRobots; }

    public int getNumRobots() { return numRobots; }
}
