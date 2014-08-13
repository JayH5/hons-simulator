package za.redbridge.simulator.config;

import ec.util.MersenneTwisterFast;
import org.jbox2d.dynamics.World;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.Int2D;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.Phenotype;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static za.redbridge.simulator.Utils.randomRange;

public class DebugSensorRobotFactory implements RobotFactory {
    protected double mass;
    protected double radius;
    protected Paint paint;
    private final Int2D envSize;
    protected Phenotype phenotype;
    private static final int PLACEMENT_DISTANCE = 10;
    private long seed;

    public DebugSensorRobotFactory(Phenotype phenotype, double mass, double radius, Paint paint, Int2D envSize, long seed) {
        this.phenotype = phenotype;
        this.mass = mass;
        this.radius = radius;
        this.paint = paint;
        this.envSize = envSize;
        this.seed = seed;
    }

    @Override
    public List<RobotObject> createInstances(World world, int number) {
        List<RobotObject> result = new ArrayList<RobotObject>(number);
        RobotObject r1 = new RobotObject(world, new Double2D(50,50), radius, mass, paint, phenotype.clone());
        RobotObject r2 = new RobotObject(world, new Double2D(70,50), radius, mass, paint, phenotype.clone());
        result.add(r1);
        result.add(r2);
        return result;
    }
}
