package za.redbridge.simulator.object;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import sim.engine.SimState;
import za.redbridge.simulator.Utils;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.phenotype.ScoreKeepingController;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.physics.Collideable;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shsu on 2014/08/13.
 */
public class TargetAreaObject extends PhysicalObject implements Collideable {

    private static final boolean ALLOW_REMOVAL = true;
    private static final float BLAME_BOX_EXPANSION_RATE = 1.05f;
    private static final int BLAME_BOX_TRIES = 5;

    private int width, height;
    private final AABB aabb;

    //hash set so that object values only get added to forage area once
    private final Set<ResourceObject> containedObjects = new HashSet<>();
    private final List<Fixture> watchedFixtures = new ArrayList<>();

    private final SimConfig simConfig;

    //keeps track of what has been pushed into this place
    public TargetAreaObject(World world, Vec2 position, int width, int height, SimConfig simConfig) {
        super(createPortrayal(width, height), createBody(world, position, width, height));

        this.width = width;
        this.height = height;

        aabb = getBody().getFixtureList().getAABB(0);

        this.simConfig = simConfig;
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

                //adjust value of the resource
                double resourceValue = resource.getMaxValue() -
                        0.9*((resource.getMaxValue()/simConfig.getSimulationIterations())*simState.schedule.getSteps());
                
                resource.setValue(resourceValue);
                addResource(resource);
            } else if (ALLOW_REMOVAL) {
                // Object moved out of completely being within the target area
                removeResource(resource);
            }
        }
    }

    private void addResource(ResourceObject resource) {
        if (containedObjects.add(resource)) {
            // Get the robots joined to the resource
            Set<RobotObject> pushingBots = new HashSet<>();

            pushingBots.addAll(resource.getPushingBots().keySet());

            // Check which robots pushed the resource in based on a bounding box
            Fixture resourceFixture = resource.getBody().getFixtureList();
            AABB resourceBox = resourceFixture.getAABB(0);
            RobotObjectQueryCallback callback = new RobotObjectQueryCallback(pushingBots);
            this.getBody().getWorld().queryAABB(callback, resourceBox);

            // If no robots joined, get nearby robots
            if (pushingBots.isEmpty()) {
                pushingBots = findRobotsNearResource(resource);
            }

            // Update the fitness for the bots involved
            if (!pushingBots.isEmpty()) {
                // Update phenotype fitness values
                updateScores(resource, pushingBots);
            }

            // Mark resource as collected (this breaks the joints)
            resource.setCollected(true);
            resource.getPortrayal().setPaint(Color.CYAN);

        }
    }

    private void removeResource(ResourceObject resource) {
        if (containedObjects.remove(resource)) {
            // Mark resource as no longer collected
            resource.setCollected(false);
            resource.getPortrayal().setPaint(Color.MAGENTA);

            Set<RobotObject> pushingBots = findRobotsNearResource(resource);

            // Check which robots pushed the resource out based on a bounding box
            Fixture resourceFixture = resource.getBody().getFixtureList();
            AABB resourceBox = resourceFixture.getAABB(0);
            RobotObjectQueryCallback callback = new RobotObjectQueryCallback(pushingBots);
            getBody().getWorld().queryAABB(callback, resourceBox);

            // Update phenotype fitness values
            penaliseBots(resource, pushingBots);
        }
    }

    //updates the scores of those robots pushing the boxes
    private void updateScores(ResourceObject resource, Set<RobotObject> pushingBots) {

        int numPushingBots = pushingBots.size();
        double value = resource.getValue()/numPushingBots;
        double cooperativeScore = numPushingBots > 1? value : 0;

        for (RobotObject bot: pushingBots) {

            ScoreKeepingController scoreKeepingIndividual = bot.getPhenotype().getController();
            scoreKeepingIndividual.incrementTotalTaskScore(value);
        }
    }

    /*
     * Finds robots very close to the ResourceObject that can be blamed for pushing the resource
     * in/out of target area.
     */
    private Set<RobotObject> findRobotsNearResource(ResourceObject resource) {
        // Check which robots pushed the resource out based on a bounding box
        Fixture resourceFixture = resource.getBody().getFixtureList();
        AABB resourceBox = resourceFixture.getAABB(0);

        // Try query robots within the AABB of the resource
        Set<RobotObject> robots = new HashSet<>();
        RobotObjectQueryCallback callback = new RobotObjectQueryCallback(robots);
        getBody().getWorld().queryAABB(callback, resourceBox);

        if (!robots.isEmpty()) {
            return robots;
        }

        // If no robots found, iteratively expand the dimensions of the query box
        AABB blameBox = new AABB(resourceBox);
        for (int i = 0; i < BLAME_BOX_TRIES; i++) {
            float width = (blameBox.upperBound.x - blameBox.lowerBound.x)
                    * BLAME_BOX_EXPANSION_RATE;
            float height = (blameBox.upperBound.y - blameBox.upperBound.y)
                    * BLAME_BOX_EXPANSION_RATE;
            Utils.resizeAABB(blameBox, width, height);
            getBody().getWorld().queryAABB(callback, blameBox);

            if (!robots.isEmpty()) {
                break;
            }
        }
        return robots;
    }

    //penalise bots
    private void penaliseBots(ResourceObject resource, Set<RobotObject> bots) {

        double totalArea = resource.getValue();
        int numPushingBots = bots.size();

        for (RobotObject bot: bots) {

            ScoreKeepingController scoreKeepingIndividual = bot.getPhenotype().getController();
            scoreKeepingIndividual.incrementTotalTaskScore(-(totalArea/numPushingBots));
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
            removeResource(resource);
        }
    }

    private static class RobotObjectQueryCallback implements QueryCallback {

        final Set<RobotObject> robots;
        RobotObjectQueryCallback(Set<RobotObject> robots) {
            this.robots = robots;
        }

        @Override
        public boolean reportFixture(Fixture fixture) {
            if (!fixture.isSensor()) { // Don't detect robot sensors, only bodies
                Object bodyUserData = fixture.getBody().getUserData();
                if (bodyUserData instanceof RobotObject) {
                    robots.add((RobotObject) bodyUserData);
                }

            }

            return true;
        }
    }
}
