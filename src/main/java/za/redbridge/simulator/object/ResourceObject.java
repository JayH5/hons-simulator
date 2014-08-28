package za.redbridge.simulator.object;

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.WeldJointDef;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import sim.engine.SimState;
import sim.util.Double2D;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject extends PhysicalObject {

    private static final Paint DEFAULT_COLOUR = new Color(255, 235, 82);

    private enum Side {
        LEFT, RIGHT, TOP, BOTTOM
    }
    private Side stickySide;

    private final double width;
    private final double height;
    private final double value;
    private final int pushingRobots;

    private boolean isCollected = false;

    private Map<RobotObject, JointDef> pendingJoints;
    private Map<RobotObject, Joint> joints;

    public ResourceObject(World world, Double2D position, double width, double height, double mass,
                          double value, int pushingRobots) {
        super(createPortrayal(width, height),
                createBody(world, position, width, height, mass));
        this.width = width;
        this.height = height;
        this.value = value;
        this.pushingRobots = pushingRobots;


        joints = new HashMap<>(pushingRobots);
        pendingJoints = new HashMap<>(pushingRobots);
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

        if (!pendingJoints.isEmpty()) {
            for (Map.Entry<RobotObject, JointDef> entry : pendingJoints.entrySet()) {
                Joint joint = getBody().getWorld().createJoint(entry.getValue());
                joints.put(entry.getKey(), joint);
            }
            pendingJoints.clear();
        }
    }

    //returns whether or not robot trying to do a pickup should move to a better side
    public boolean tryPickup(RobotObject robot) {
        if (isCollected) {
            return false;
        }

        // Check if max number of robots already attached
        if (pushedByMaxRobots()) {
            return false;
        }

        // Check if robot not already attached or about to be attached
        if (joints.containsKey(robot) || pendingJoints.containsKey(robot)) {
            return false;
        }

        // Check the side that the robot wants to attach to
        // If it is not the sticky side don't allow it to attach
        Body robotBody = robot.getBody();
        final Side attachSide = getSideClosestToPoint(robotBody.getPosition());
        if (stickySide != null && stickySide != attachSide) {
            return true;
        }

        // Set the sticky side
        if (stickySide == null) {
            stickySide = attachSide;
        }

        // Create the joint definition
        WeldJointDef wjd = new WeldJointDef();
        wjd.bodyA = getBody();
        wjd.bodyB = robotBody;

        wjd.referenceAngle = getReferenceAngleForSide(attachSide);

        wjd.localAnchorA.set(getAnchorPointForSide(attachSide));
        wjd.localAnchorB.set(robot.getRadius() + 0.01f, 0f); // Attach to front of robot

        wjd.collideConnected = true;

        pendingJoints.put(robot, wjd);

        // Mark the robot as bound
        robot.setBoundToResource(true);

        return false;
    }

    private Side getSideClosestToPoint(Vec2 point) {
        Vec2 relativePoint = Transform.mulTrans(getBody().getTransform(), point);
        final Side side;
        if (Math.abs(relativePoint.x) > Math.abs(relativePoint.y)) {
            if (relativePoint.x > 0) {
                side = Side.RIGHT;
            } else {
                side = Side.LEFT;
            }
        } else {
            if (relativePoint.y > 0) {
                side = Side.BOTTOM;
            } else {
                side = Side.TOP;
            }
        }
        return side;
    }

    private Vec2 getAnchorPointForSide(Side side) {
        int position = joints.size() + pendingJoints.size();
        Vec2 anchorPoint;
        if (side == Side.LEFT || side == Side.RIGHT) {
            float spacing = (float) (height / pushingRobots);
            float y = (float) height / 2 - (spacing * position + spacing / 2);
            float x = side == Side.LEFT ? (float) -width / 2 : (float) width / 2;
            anchorPoint = new Vec2(x, y);
        } else {
            float spacing = (float) (width / pushingRobots);
            float x = (float) -width / 2 + spacing * position + spacing / 2;
            float y = side == Side.BOTTOM ? (float) -height / 2 : (float) height / 2;
            anchorPoint = new Vec2(x, y);
        }

        return anchorPoint;
    }

    private float getReferenceAngleForSide(Side side) {
        final float referenceAngle;
        if (side == Side.LEFT) {
            referenceAngle = 0f;
        } else if (side == Side.RIGHT) {
            referenceAngle = (float) Math.PI;
        } else if (side == Side.TOP) {
            referenceAngle = (float) -Math.PI / 2;
        } else {
            referenceAngle = (float) Math.PI / 2;
        }
        return referenceAngle;
    }
    
    public Vec2 getStickySideAttachmentPoint() {
        int position = joints.size() + pendingJoints.size();
        Vec2 anchorPoint;
        if (stickySide == Side.LEFT || stickySide == Side.RIGHT) {
            float spacing = (float) (height / pushingRobots);
            float y = (float) height / 2 - (spacing * position + spacing / 2);
            float x = stickySide == Side.LEFT ? (float) -width / 2 : (float) width / 2;
            anchorPoint = new Vec2(x, y);
        } else {
            float spacing = (float) (width / pushingRobots);
            float x = (float) -width / 2 + spacing * position + spacing / 2;
            float y = stickySide == Side.BOTTOM ? (float) -height / 2 : (float) height / 2;
            anchorPoint = new Vec2(x, y);
        }

        return anchorPoint;
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

    public boolean pushedByMaxRobots() {
        return joints.size() + pendingJoints.size() >= pushingRobots;
    }

    private void breakRobotWeldJoint() {
        for (Map.Entry<RobotObject, Joint> entry: joints.entrySet()) {
            RobotObject robot = entry.getKey();
            robot.setBoundToResource(false);
            getBody().getWorld().destroyJoint(entry.getValue());
        }
        joints.clear();
    }

    public boolean isPushed() { return !joints.isEmpty(); }

}
