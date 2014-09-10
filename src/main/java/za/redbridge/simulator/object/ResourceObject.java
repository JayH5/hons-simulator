package za.redbridge.simulator.object;

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
            // Create all the pending joints and then clear them
            for (Map.Entry<RobotObject, JointDef> entry : pendingJoints.entrySet()) {
                Joint joint = getBody().getWorld().createJoint(entry.getValue());
                joints.put(entry.getKey(), joint);
            }
            pendingJoints.clear();
        }
    }


    /**
     * Try join this resource to the provided robot. If successful, a weld joint will be created
     * between the resource and the robot and the method will return true.
     * @param robot The robot trying to pick up this resource
     * @return true if the pickup attempt was successful
     */
    public boolean tryPickup(RobotObject robot) {
        // Check if already collected or max number of robots already attached
        if (isCollected || pushedByMaxRobots()) {
            //System.out.println("Pickup failed: is collected.");
            return false;
        }

        // Check if robot not already attached or about to be attached
        if (joints.containsKey(robot) || pendingJoints.containsKey(robot)) {
            //System.out.println("Pickup failed: about to be joined.");
            return false;
        }

        // Check the side that the robot wants to attach to
        // If it is not the sticky side don't allow it to attach
        Body robotBody = robot.getBody();
        Vec2 robotPosition = robotBody.getPosition();
        final Side attachSide = getSideClosestToPoint(robotPosition);
        if (stickySide != null && stickySide != attachSide) {
            //System.out.println("Pickup failed: wrong sticky side.");
            return false;
        }

        if (stickySide == null) {
            setStickySide(attachSide);
        }

        //check if the anchor point is too far away from the robot
        AnchorPoint closestAnchor = getClosestAnchorPoint(robotPosition);
        double dist = getBody().getWorldPoint(closestAnchor.position).sub(robotPosition).length();

        if (dist > robot.getRadius() * 1.5) {
            //System.out.println("Pickup failed: too far away.");
            return false;
        }

        createPendingWeldJoint(robot, closestAnchor.position);

        // Mark the anchor as taken and the robot as bound to a resource.
        closestAnchor.markTaken();
        robot.setBoundToResource(true);

        return true;
    }

    /**
     * Creates a weld joint definition between the resource and the robot and adds it to the set of
     * pending joints to be created.
     * @param robot The robot to weld to
     * @param anchorPoint The local point on the resource to create the weld
     */
    private void createPendingWeldJoint(RobotObject robot, Vec2 anchorPoint) {
        WeldJointDef wjd = new WeldJointDef();
        wjd.bodyA = getBody();
        wjd.bodyB = robot.getBody();
        wjd.referenceAngle = getReferenceAngle();
        wjd.localAnchorA.set(anchorPoint);
        wjd.localAnchorB.set(robot.getRadius() + 0.01f, 0); // Attach to front of robot
        wjd.collideConnected = true;

        pendingJoints.put(robot, wjd);
    }

    /**
     * Get the side of this resource closest to the given point.
     * @param point A point in world-space
     * @return the side closest to the given point
     */
    public Side getSideClosestToPoint(Vec2 point) {
        Vec2 localPoint = getBody().getLocalPoint(point);
        final Side side;
        if (Math.abs(localPoint.x) > Math.abs(localPoint.y)) {
            if (localPoint.x > 0) {
                side = Side.RIGHT;
            } else {
                side = Side.LEFT;
            }
        } else {
            if (localPoint.y > 0) {
                side = Side.TOP;
            } else {
                side = Side.BOTTOM;
            }
        }
        return side;
    }

    /** Get the side that robots can currently attach to. */
    public Side getStickySide () {
        return stickySide;
    }

    /* Initializes the anchor points after the sticky side has been determined */
    private void setStickySide(Side stickySide) {
        if (this.stickySide != null) {
            throw new IllegalStateException("Sticky side already set");
        }

        this.stickySide = stickySide;

        // Initialize the anchor points
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

    /**
     * Get the position of the closest anchor point in world coordinates, or null if all anchor
     * points have been taken.
     * @param position A position in world coordinates
     * @return The position of the closest available anchor point, or null if none is available.
     */
    public Vec2 getClosestAnchorPosition(Vec2 position) {
        AnchorPoint localAnchorPoint = getClosestAnchorPoint(position);
        if (localAnchorPoint != null) {
            return getBody().getWorldPoint(localAnchorPoint.position);
        }
        return null;
    }

    /**
     * Get the closest anchor point to a position in world space, or null if none is available.
     * @param position point in world coordinates
     * @return an {@link AnchorPoint} object that has not been taken yet, or null if unavailable
     */
    private AnchorPoint getClosestAnchorPoint(Vec2 position) {
        // If sticky side not set yet then anchor points not available
        if (stickySide == null) {
            return null;
        }

        // Fast path for single robot resource
        if (pushingRobots == 1) {
            if (!anchorPoints[0].taken) {
                return anchorPoints[0];
            } else {
                return null;
            }
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

    /** Get the reference angle for joints for the current sticky side. */
    private float getReferenceAngle() {
        final float referenceAngle;
        if (stickySide == Side.LEFT) {
            referenceAngle = 0f;
        } else if (stickySide == Side.RIGHT) {
            referenceAngle = (float) Math.PI;
        } else if (stickySide == Side.BOTTOM) {
            referenceAngle = (float) Math.PI / 2;
        } else if (stickySide == Side.TOP) {
            referenceAngle = (float) -Math.PI / 2;
        } else {
            throw new IllegalStateException("Sticky side not set yet, cannot get reference angle");
        }
        return referenceAngle;
    }

    /**
     * Check whether this object has been collected
     * @return true if the object has been marked as collected (it has passed into the target area)
     */
    public boolean isCollected() {
        return isCollected;
    }

    /** Mark this object as collected. i.e. mark it as being in the target area. */
    public void markCollected() {
        this.isCollected = true;

        // Break all the joints
        for (Map.Entry<RobotObject, Joint> entry: joints.entrySet()) {
            RobotObject robot = entry.getKey();
            robot.setBoundToResource(false);
            getBody().getWorld().destroyJoint(entry.getValue());
        }
        joints.clear();
    }

    /** Check whether this resource already has the max number of robots attached to it. */
    public boolean pushedByMaxRobots() {
        return getNumberPushingRobots() >= pushingRobots;
    }

    /** Get the number of robots currently pushing/attached to this resource. */
    public int getNumberPushingRobots() {
        return joints.size() + pendingJoints.size();
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getDiagonalLength() {
        return Math.hypot(width, height);
    }

    /*
     * Container class for points along the sticky edge of the resource where robots can attach to
     * the resource.
     */
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

    /*
     * Simple portrayal for drawing an additional line along the bottom of the resource to help
     * determine which way round the resource is.
     */
    private class DebugPortrayal extends PolygonPortrayal {

        public DebugPortrayal(Paint paint, boolean filled) {
            super(4, paint, filled);

            final float width = (float) getWidth() * 0.8f;
            final float height = (float) getHeight() * 0.1f;

            final float dy = (float) getHeight() * 0.3f;

            float halfWidth = width / 2;
            float halfHeight = height / 2;
            vertices[0].set(-halfWidth, -halfHeight - dy);
            vertices[1].set(halfWidth, -halfHeight - dy);
            vertices[2].set(halfWidth, halfHeight - dy);
            vertices[3].set(-halfWidth, halfHeight - dy);
        }
    }

}
