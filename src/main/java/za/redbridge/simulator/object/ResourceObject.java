package za.redbridge.simulator.object;

import org.jbox2d.collision.WorldManifold;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.WeldJointDef;

import java.awt.Color;
import java.awt.Paint;

import sim.engine.SimState;
import sim.util.Double2D;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.physics.Collideable;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject extends PhysicalObject implements Collideable {

    private static final Paint DEFAULT_COLOUR = new Color(255, 235, 82);

    private final double value;

    private JointDef pendingJoint = null;
    private Joint robotJoint = null;

    private boolean isCollected = false;

    private double width, height;

    public ResourceObject(World world, Double2D position, double width, double height, double mass,
                          double value) {
        super(createPortrayal(width, height),
                createBody(world, position, width, height, mass));
        this.value = value;
        this.width = width;
        this.height = height;
    }

    protected static Portrayal createPortrayal(double width, double height) {
        return new RectanglePortrayal(width, height, DEFAULT_COLOUR, true);
    }

    protected static Body createBody(World world, Double2D position, double width, double height,
                                     double mass) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.DYNAMIC)
                .setPosition(position)
                .setRectangular(width, height, mass)
                .setFriction(0.9f)
                .setRestitution(1.0f)
                .build(world);
    }

    public double getValue() {
        return value;
    }

    @Override
    public void step(SimState simState) {
        super.step(simState);
        if (pendingJoint != null) {
            robotJoint = getBody().getWorld().createJoint(pendingJoint);
            pendingJoint = null;
        }
    }

    @Override
    public void handleBeginContact(Contact contact, Fixture otherFixture) {
        if (pendingJoint != null || robotJoint != null || isCollected) {
            return;
        }

        // Get the robot and make sure it's not already bound to a resource
        RobotObject robot = (RobotObject) otherFixture.getBody().getUserData();
        if (robot.isBoundToResource()) {
            return;
        }

        // Bind to the robot by creating a weld
        // Cheat by making the bind point the
        if (contact.getManifold().pointCount < 1) {
            return;
        }

        Body resourceBody = getBody();
        Body robotBody = robot.getBody();

        WorldManifold manifold = new WorldManifold();
        contact.getWorldManifold(manifold);
        Vec2 collisionPoint = manifold.points[0];

        WeldJointDef wjd = new WeldJointDef();
        wjd.initialize(resourceBody, robotBody, collisionPoint);
        wjd.collideConnected = true;
        //wjd.referenceAngle = robotBody.getAngle() - resourceBody.getAngle();
        pendingJoint = wjd;

        // Mark the robot as bound
        robot.setBoundToResource(true);
    }

    //works out if an attachment is happening on a 'valid' side of the resource
    public boolean isValidAttachment (Body resourceBody, Vec2 anchor) {

        Vec2 anchorOnResource = new Vec2();

        resourceBody.getLocalPointToOut(anchor, anchorOnResource);

        double orientation = body.getAngle()% (2*Math.PI);

        body.getLocalPoint(anchor);

        return true;

    }

    @Override
    public void handleEndContact(Contact contact, Fixture otherFixture) {
        // Nothing to do
    }

    @Override
    public boolean isRelevantObject(Fixture otherFixture) {
        // Only care about collisions with agents
        return otherFixture.getBody().getUserData() instanceof RobotObject
                && otherFixture.getUserData() == null;
    }

    /**
     * Check whether this object has been collected
     * @return true if the object is in the target area
     */
    public boolean isCollected() {
        return isCollected;
    }

    /**
     * Mark this object as collected. i.e. mark it as being in the target area
     */
    public void markCollected() {
        this.isCollected = true;
        breakRobotWeldJoint();
    }

    private void breakRobotWeldJoint() {
        if (robotJoint != null) {
            RobotObject robot = (RobotObject) robotJoint.getBodyB().getUserData();
            robot.setBoundToResource(false);
            getBody().getWorld().destroyJoint(robotJoint);
            robotJoint = null;
        }
    }
}
