package za.redbridge.simulator.object;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import java.awt.Color;
import java.awt.Paint;
import java.util.*;
import java.util.stream.Collectors;

import sim.engine.SimState;
import za.redbridge.simulator.FitnessStats;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.physics.Collideable;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Created by shsu on 2014/08/13.
 */
public class TargetAreaObject extends PhysicalObject implements Collideable {

    private static final boolean ALLOW_REMOVAL = true;

    private int width, height;
    private final AABB aabb;

    //total resource value in this target area
    private Map<Phenotype,FitnessStats> fitnesses = new HashMap<>();

    //hash set so that object values only get added to forage area once
    private final Set<ResourceObject> containedObjects = new HashSet<>();
    private final List<Fixture> watchedFixtures = new ArrayList<>();

    //keeps track of what has been pushed into this place
    public TargetAreaObject(World world, Vec2 position, int width, int height) {
        super(createPortrayal(width, height), createBody(world, position, width, height));

        this.width = width;
        this.height = height;

        aabb = getBody().getFixtureList().getAABB(0);
    }

    protected static Portrayal createPortrayal(int width, int height) {
        Paint areaColour = new Color(31, 110, 11, 100);
        return new RectanglePortrayal(width, height, areaColour, true);
    }

    protected static Body createBody(World world, Vec2 position, int width, int height) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.STATIC)
                .setPosition(position)
                .setRectangular(width, height)
                .setSensor(true)
                .setFilterCategoryBits(FilterConstants.CategoryBits.TARGET_AREA)
                .setFilterMaskBits(FilterConstants.CategoryBits.RESOURCE
                        | FilterConstants.CategoryBits.TARGET_AREA_SENSOR)
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
                    List<Phenotype> pushingPhenotypes = resource.getPushingRobots().stream().map(j -> j.getPhenotype()).collect(Collectors.toList());
                    for(Phenotype p : pushingPhenotypes){
                        fitnesses.putIfAbsent(p, new FitnessStats());
                        FitnessStats stats = fitnesses.get(p);
                        stats.addTaskFitness(resource.getValue() / pushingPhenotypes.size());
                        stats.addRetrievedResource(resource, pushingPhenotypes.size());
                    }
                    resource.setCollected(true);
                    resource.getPortrayal().setPaint(Color.CYAN);
                }
            } else if (ALLOW_REMOVAL) {
                // Object moved out of completely being within the target area
                if (containedObjects.remove(resource)) {
                    resource.setCollected(false);
                    resource.getPortrayal().setPaint(Color.MAGENTA);
                    List<Phenotype> pushingPhenotypes = new ArrayList<>(); //TODO actually find the phenotypes that pushed the box out
                    for(Phenotype p : pushingPhenotypes){
                        fitnesses.putIfAbsent(p, new FitnessStats());
                        FitnessStats stats = fitnesses.get(p);
                        stats.addTaskFitness(-resource.getValue() / pushingPhenotypes.size());
                    }
                }
            }
        }
    }

    public int getNumberOfContainedResources() {
        return containedObjects.size();
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

    public Map<Phenotype, FitnessStats> getFitnesses() {
        return fitnesses;
    }

    @Override
    public void handleBeginContact(Contact contact, Fixture otherFixture) {
        if (!(otherFixture.getBody().getUserData() instanceof ResourceObject)) {
            return;
        }

        if (!watchedFixtures.contains(otherFixture)) {
            watchedFixtures.add(otherFixture);
        }
    }

    @Override
    public void handleEndContact(Contact contact, Fixture otherFixture) {
        if (!(otherFixture.getBody().getUserData() instanceof ResourceObject)) {
            return;
        }

        // Remove from watch list
        watchedFixtures.remove(otherFixture);

        // Remove from the score
        if (ALLOW_REMOVAL) {
            ResourceObject resource = (ResourceObject) otherFixture.getBody().getUserData();
            if (containedObjects.remove(resource)) {
                resource.setCollected(false);
                resource.getPortrayal().setPaint(Color.MAGENTA);
                List<Phenotype> pushingPhenotypes = new ArrayList<>(); //TODO actually find the phenotypes that pushed the box out
                for(Phenotype p : pushingPhenotypes){
                    fitnesses.putIfAbsent(p, new FitnessStats());
                    FitnessStats stats = fitnesses.get(p);
                    stats.addTaskFitness(-resource.getValue() / pushingPhenotypes.size());
                }
            }
        }
    }

}
