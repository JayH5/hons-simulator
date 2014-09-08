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
import java.util.Optional;

import sim.engine.SimState;
import sim.util.Double2D;
import za.redbridge.simulator.physics.BodyBuilder;
import za.redbridge.simulator.portrayal.PolygonPortrayal;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

/**
 * Object to represent the resources in the environment. Has a value and a weight.
 *
 * Created by jamie on 2014/07/23.
 */
public class ResourceObject extends PhysicalObject {

    private static final Paint DEFAULT_COLOUR = new Color(255, 235, 82);
    private static final boolean DEBUG = true;

    public enum Side {

        LEFT, RIGHT, TOP, BOTTOM
    }

    private Side stickySide;

    private final double width;
    private final double height;
    private final int pushingRobots;

    private boolean isCollected = false;

    private final AnchorPoint[] anchorPoints;

    private final Map<RobotObject, JointDef> pendingJoints;
    private final Map<RobotObject, Joint> joints;

    public ResourceObject(World world, Double2D position, double width, double height, double mass,
                          int pushingRobots) {
        super(createPortrayal(width, height),
                createBody(world, position, width, height, mass));
        this.width = width;
        this.height = height;
        this.pushingRobots = pushingRobots;

        anchorPoints = new AnchorPoint[pushingRobots];

        joints = new HashMap<>(pushingRobots);
        pendingJoints = new HashMap<>(pushingRobots);

        if (DEBUG) {
            getPortrayal().setChildDrawable(new DebugPortrayal(Color.BLACK, true));
        }
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
                .setFriction(2.9f)
                .setRestitution(0.4f)
                .build(world);
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

    //returns closest attachment point to robot (global) if attachment fails because bot is on wrong side.
    //otherwise, it returns a vector with a negative x value for other failures, negative y for success.
    public Vec2 tryPickup(RobotObject robot) {
        if (isCollected) {
            return new Vec2(-1, 0);
        }

        // Check if max number of robots already attached
        if (pushedByMaxRobots()) {
            return new Vec2(-2, 0);
        }

        // Check if robot not already attached or about to be attached
        if (joints.containsKey(robot) || pendingJoints.containsKey(robot)) {
            return new Vec2(-3, 0);
        }

        // Check the side that the robot wants to attach to
        // If it is not the sticky side don't allow it to attach
        Body robotBody = robot.getBody();
        Vec2 robotPosition = robotBody.getPosition();
        final Side attachSide = getSideClosestToPoint(robotPosition);
        if (stickySide != null && stickySide != attachSide) {
            return getBody().getWorldPoint(getClosestAnchorPoint(robotPosition));
        }


        // Set the sticky side
        if (stickySide == null) {
            stickySide = attachSide;
            createAnchorPoints();
        }


        //check if the anchor point is too far away from the robot

        double dist = getBody().getWorldPoint(getClosestAnchorPoint(robotPosition)).sub(robot.getBody().getPosition()).length();

        if (dist > robot.getRadius()*1.5) {
            System.out.println("Pickup failed: too far away.");
            return getBody().getWorldPoint(getClosestAnchorPoint(robotPosition));
        }

        // Create the joint definition
        WeldJointDef wjd = new WeldJointDef();
        wjd.bodyA = getBody();
        wjd.bodyB = robotBody;

        wjd.referenceAngle = getReferenceAngle();

        AnchorPoint closestAnchor = getClosestAnchor(robotPosition);
        wjd.localAnchorA.set(closestAnchor.position);

        closestAnchor.markTaken();

        wjd.localAnchorB.set(robot.getRadius() + 0.01f, 0f); // Attach to front of robot

        wjd.collideConnected = true;

        pendingJoints.put(robot, wjd);

        // Mark the robot as bound
        robot.setBoundToResource(true);

        return new Vec2(0, -1);
    }

    public double getHypot() {
        return Math.sqrt(width*width + height*height);
    }

    public Side getStickySide () {

        return stickySide;
    }

    //in: world
    public Side getSideClosestToPoint(Vec2 point) {
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

    private void createAnchorPoints() {
        for (int i = 0; i < pushingRobots; i++) {
            Vec2 position;
            if (stickySide == Side.LEFT || stickySide == Side.RIGHT) {
                float spacing = (float) (height / pushingRobots);
                float y = (float) height / 2 - (spacing * i + spacing / 2);
                float x = stickySide == Side.LEFT ? (float) -width / 2 : (float) width / 2;
                position = new Vec2(x, y);
            } else {
                float spacing = (float) (width / pushingRobots);
                float x = (float) -width / 2 + spacing * i + spacing / 2;
                float y = stickySide == Side.TOP ? (float) -height / 2 : (float) height / 2;
                position = new Vec2(x, y);
            }
            anchorPoints[i] = new AnchorPoint(position);
        }
    }

    //return global coord
    public Vec2 getClosestAnchorPointWorld(Vec2 position) {
        return this.getBody().getWorldPoint(getClosestAnchorPoint(position));
    }

    //in: world out: local
    private Vec2 getClosestAnchorPoint(Vec2 position) {
        // Fast path for single robot resource
        if (pushingRobots == 1) {
            return anchorPoints[0].position;
        }

        // Else iterate through anchor points finding closest one (generally only 2 options)
        Vec2 closestAnchorPoint = null;
        float shortestDistance = Float.MAX_VALUE;
        for (int i = 0; i < pushingRobots; i++) {
            AnchorPoint anchorPoint = anchorPoints[i];
            if (anchorPoint.taken) {
                continue;
            }

            float distance = getBody().getPosition().sub(position).lengthSquared();
            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestAnchorPoint = anchorPoint.position;
            }
        }

        return closestAnchorPoint;
    }

    private AnchorPoint getClosestAnchor(Vec2 position) {
        // Fast path for single robot resource
        if (pushingRobots == 1) {
            return anchorPoints[0];
        }

        // Else iterate through anchor points finding closest one (generally only 2 options)
        AnchorPoint closestAnchorPoint = null;
        float shortestDistance = Float.MAX_VALUE;
        for (int i = 0; i < pushingRobots; i++) {
            AnchorPoint anchorPoint = anchorPoints[i];
            if (anchorPoint.taken) {
                continue;
            }

            float distance = getBody().getPosition().sub(position).lengthSquared();
            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestAnchorPoint = anchorPoint;
            }
        }

        return closestAnchorPoint;
    }

    private float getReferenceAngle() {
        final float referenceAngle;
        if (stickySide == Side.LEFT) {
            referenceAngle = 0f;
        } else if (stickySide == Side.RIGHT) {
            referenceAngle = (float) Math.PI;
        } else if (stickySide == Side.TOP) {
            referenceAngle = (float) Math.PI / 2;
        } else {
            referenceAngle = (float) -Math.PI / 2;
        }
        return referenceAngle;
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

    public int getNumPushingBots() { return joints.size(); }

    private void breakRobotWeldJoint() {
        for (Map.Entry<RobotObject, Joint> entry: joints.entrySet()) {
            RobotObject robot = entry.getKey();
            robot.setBoundToResource(false);
            getBody().getWorld().destroyJoint(entry.getValue());
        }
        joints.clear();
    }

    public boolean isPushed() { return !joints.isEmpty(); }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    private static class AnchorPoint {
        final Vec2 position;
        boolean taken = false;

        private AnchorPoint(Vec2 position) {
            this.position = position;
        }

        private void markTaken() {
            taken = true;
        }
    }


    private class DebugPortrayal extends PolygonPortrayal {

        public DebugPortrayal(Paint paint, boolean filled) {
            super(4, paint, filled);

            final float width = (float) getWidth() * 0.8f;
            final float height = (float) getHeight() * 0.1f;

            final float dy = (float) getHeight() * 0.3f;

            float halfWidth = width / 2;
            float halfHeight = height / 2;
            vertices[0].set(-halfWidth, -halfHeight + dy);
            vertices[1].set(halfWidth, -halfHeight + dy);
            vertices[2].set(halfWidth, halfHeight + dy);
            vertices[3].set(-halfWidth, halfHeight + dy);
        }
    }

}
