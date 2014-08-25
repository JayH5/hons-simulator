package za.redbridge.simulator.object;

import org.jbox2d.collision.AABB;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sim.engine.SimState;
import sim.util.Double2D;
import za.redbridge.simulator.ea.FitnessFunction;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Created by shsu on 2014/08/13.
 */
public class TargetAreaObject extends PhysicalObject {

    private static final boolean ALLOW_REMOVAL = true;

    private int width, height;
    private final AABB aabb;

    private FitnessFunction fitnessFunction;

    //total value of resources within this forage area
    private double totalObjectValue;

    //total fitness value for the agents in this simulation. unfortunately fitness is dead tied to forage area and
    //how much stuff is in there.
    private double totalFitness;

    //hash set so that object values only get added to forage area once
    private final Set<ResourceObject> containedObjects = new HashSet<>();
    private final List<Fixture> watchedFixtures = new ArrayList<>();

    //keeps track of what has been pushed into this place

    public TargetAreaObject(World world, Double2D pos, int width, int height, FitnessFunction fitnessFunction) {
        super(createPortrayal(width, height), createBody(world, pos, width, height));

        this.fitnessFunction = fitnessFunction;
        totalObjectValue = 0;
        totalFitness = 0;
        this.width = width;
        this.height = height;

        aabb = getBody().getFixtureList().getAABB(0);
    }

    protected static Portrayal createPortrayal(int width, int height) {
        Paint areaColour = new Color(31, 110, 11, 100);
        return new RectanglePortrayal(width, height, areaColour, true);
    }

    protected static Body createBody(World world, Double2D position, int width, int height) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.STATIC)
                .setPosition(position)
                .setRectangular(width, height)
                .setSensor(true)
                .build(world);
    }

    @Override
    public void step(SimState simState) {
        super.step(simState);

        // Check if any objects have passed into the target area completely or have left
        for (Fixture fixture : watchedFixtures) {
            ResourceObject resource = (ResourceObject) fixture.getBody().getUserData();
            if (aabb.contains(fixture.getAABB(0))) {
                // Object moved completely into the target area
                if (containedObjects.add(resource)) {
                    resource.getPortrayal().setPaint(Color.CYAN);
                    incrementTotalObjectValue(resource.getValue());
                }
            } else if (ALLOW_REMOVAL) {
                // Object moved out of completely being within the target area
                if (containedObjects.remove(resource)) {
                    resource.getPortrayal().setPaint(Color.MAGENTA);
                    decrementTotalObjectValue(resource.getValue());
                }
            }
        }
    }

    //these also update the overall fitness value
    public void setTotalObjectValue(double totalObjectValue) {
        this.totalObjectValue = totalObjectValue;
        totalFitness = fitnessFunction.calculateFitness(this.totalObjectValue);
    }

    private void incrementTotalObjectValue(double value) {
        totalObjectValue += value;
        totalFitness = fitnessFunction.calculateFitness(totalObjectValue);
    }

    private void decrementTotalObjectValue(double value) {
        totalObjectValue -= value;
        totalFitness = fitnessFunction.calculateFitness(totalObjectValue);
    }

    public double getTotalFitness() {
        return totalFitness;
    }

    public void enterObject(Fixture fixture) {
        Object fixtureBodyData = fixture.getBody().getUserData();
        if (!(fixtureBodyData instanceof ResourceObject)) {
            return;
        }

        // Add to the watch list
        if (!watchedFixtures.contains(fixture)) {
            watchedFixtures.add(fixture);
        }
    }

    public void exitObject(Fixture fixture) {
        // Check if the fixture is a resource
        Object fixtureBodyData = fixture.getBody().getUserData();
        if (!(fixtureBodyData instanceof ResourceObject)) {
            return;
        }

        // Remove from watch list
        watchedFixtures.remove(fixture);

        // Remove from the score
        if (ALLOW_REMOVAL) {
            ResourceObject resource = (ResourceObject) fixtureBodyData;
            if (containedObjects.remove(resource)) {
                decrementTotalObjectValue(resource.getValue());
            }
        }
    }

    public AABB getAabb() {
        return aabb;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
