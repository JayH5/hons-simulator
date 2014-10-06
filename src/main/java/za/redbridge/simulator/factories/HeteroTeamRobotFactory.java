package za.redbridge.simulator.factories;


import org.jbox2d.dynamics.World;
import za.redbridge.simulator.PlacementArea;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.Phenotype;

import java.awt.Paint;
import java.util.List;
import java.util.Set;

/**
 * Created by racter on 2014/10/05.
 * Creates a team of robots with the same morphologies but different controllers. for now,
 * only one copy of controller allowed in each team.
 */
public class HeteroTeamRobotFactory implements RobotFactory {

    protected double mass;
    protected double radius;
    protected Paint paint;
    protected Set<Phenotype> phenotypes;

    public HeteroTeamRobotFactory(Set<Phenotype> phenotypes, double mass, double radius, Paint paint) {
        this.phenotypes = phenotypes;
        this.mass = mass;
        this.radius = radius;
        this.paint = paint;
    }

    @Override
    public void placeInstances(PlacementArea.ForType<RobotObject> placementArea, World world,
                               SimConfig.Direction targetAreaPlacement) {

        for (Phenotype phenotype: phenotypes) {
            PlacementArea.Space space = placementArea.getRandomSpace(radius);

            Phenotype copy = phenotype.clone();

            RobotObject robot =
                    new RobotObject(world, space.getPosition(), radius, mass, paint, copy, targetAreaPlacement);

            placementArea.placeObject(space, robot);
        }
    }

    @Override
    public void setNumRobots(int numRobots) {
        //not supported
    }

    @Override
    public int getNumRobots() { return phenotypes.size(); }
}
