package za.redbridge.simulator.config;

import org.jbox2d.dynamics.World;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import ec.util.MersenneTwisterFast;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.Int2D;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.interfaces.RobotFactory;
import za.redbridge.simulator.object.RobotObject;

import static za.redbridge.simulator.Utils.randomRange;

public class HomogeneousRobotFactory implements RobotFactory {
    protected double mass;
    protected double radius;
    protected Paint paint;
    private final Int2D envSize;
    protected Phenotype phenotype;
    private static final int PLACEMENT_DISTANCE = 10;
    private long seed;

    public HomogeneousRobotFactory(Phenotype phenotype, double mass, double radius, Paint paint, Int2D envSize, long seed) {
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
        Continuous2D placementEnv = new Continuous2D(1.0, envSize.x, envSize.y);
        MersenneTwisterFast random = new MersenneTwisterFast(seed);

        for(int i=0; i < number; i++) {
            final int maxTries = 1000;
            int tries = 0;
            Double2D pos;
            do {
                if (tries++ >= maxTries) {
                    throw new RuntimeException("Unable to find space for object");
                }
                pos = new Double2D(randomRange(random, radius, envSize.x - radius), randomRange(random, radius, envSize.y - radius));
            } while (!placementEnv.getNeighborsWithinDistance(pos, PLACEMENT_DISTANCE).isEmpty());
            RobotObject r = new RobotObject(world, pos, radius, mass, paint, phenotype);
            result.add(r);

            placementEnv.setObjectLocation(r, pos);
        }
        return result;
    }
}
