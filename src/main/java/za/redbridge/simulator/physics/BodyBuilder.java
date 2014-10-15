package za.redbridge.simulator.physics;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.TopDownFrictionJointDef;

import sim.util.Double2D;


import static za.redbridge.simulator.Utils.toVec2;

/**
 * Builder API for creating Body objects with a single fixture.
 * Created by jamie on 2014/08/06.
 */
public class BodyBuilder {

    private static final float ACCELERATION_GRAVITY = 9.81f;

    private static final float DEFAULT_ANGULAR_DAMPING = .1f;
    private static final float DEFAULT_LINEAR_DAMPING = .1f;

    private final BodyDef bd = new BodyDef();
    private final FixtureDef fd = new FixtureDef();

    private boolean groundFriction = false;
    private float staticCOF;
    private float kineticCOF;
    private float staticTorqueCOF;
    private float kineticTorqueCOF;

    public BodyBuilder() {
        bd.setAngularDamping(DEFAULT_ANGULAR_DAMPING);
        bd.setLinearDamping(DEFAULT_LINEAR_DAMPING);
    }

    public BodyBuilder setBodyType(BodyType type) {
        bd.type = type;
        return this;
    }

    public BodyBuilder setPosition(Vec2 position) {
        bd.position = position;
        return this;
    }

    public BodyBuilder setPosition(Double2D position) {
        return setPosition(toVec2(position));
    }

    public BodyBuilder setPosition(float x, float y) {
        bd.position.set(x, y);
        return this;
    }

    public BodyBuilder setAngle(float angle) {
        bd.angle = angle;
        return this;
    }

    public BodyBuilder setLinearVelocity(Vec2 velocity) {
        bd.linearVelocity = velocity;
        return this;
    }

    public BodyBuilder setLinearDamping(float damping) {
        bd.linearDamping = damping;
        return this;
    }

    public BodyBuilder setAngularVelocity(float velocity) {
        bd.angularVelocity = velocity;
        return this;
    }

    public BodyBuilder setAngularDamping(float damping) {
        bd.angularDamping = damping;
        return this;
    }

    public BodyBuilder setShape(Shape shape) {
        fd.shape = shape;
        return this;
    }

    public BodyBuilder setCircular(float radius) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        fd.shape = shape;
        return this;
    }

    public BodyBuilder setCircular(double radius) {
        return setCircular((float) radius);
    }

    public BodyBuilder setCircular(float radius, float mass) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        fd.shape = shape;
        fd.density =  mass / ((float) Math.PI * radius * radius);
        return this;
    }

    public BodyBuilder setCircular(double radius, double mass) {
        return setCircular((float) radius, (float) mass);
    }

    public BodyBuilder setRectangular(float width, float height) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        fd.shape = shape;
        return this;
    }

    public BodyBuilder setRectangular(double width, double height) {
        return setRectangular((float) width, (float) height);
    }

    public BodyBuilder setRectangular(float width, float height, float mass) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        fd.shape = shape;
        fd.density = mass / (height * width);
        return this;
    }

    public BodyBuilder setRectangular(double width, double height, double mass) {
        return setRectangular((float) width, (float) height, (float) mass);
    }

    public BodyBuilder setEdge(Vec2 v1, Vec2 v2) {
        EdgeShape shape = new EdgeShape();
        shape.set(v1, v2);
        fd.shape = shape;
        return this;
    }

    public BodyBuilder setEdge(Double2D v1, Double2D v2) {
        return setEdge(toVec2(v1), toVec2(v2));
    }

    public BodyBuilder setDensity(float density) {
        fd.density = density;
        return this;
    }

    public BodyBuilder setFriction(float friction) {
        fd.friction = friction;
        return this;
    }

    public BodyBuilder setRestitution(float restitution) {
        fd.restitution = restitution;
        return this;
    }

    public BodyBuilder setSensor(boolean isSensor) {
        fd.isSensor = isSensor;
        return this;
    }

    public BodyBuilder setFilterCategoryBits(int categoryBits) {
        fd.filter.categoryBits = categoryBits;
        return this;
    }

    public BodyBuilder setFilterMaskBits(int maskBits) {
        fd.filter.maskBits = maskBits;
        return this;
    }

    public BodyBuilder setFilterGroupIndex(int groupIndex) {
        fd.filter.groupIndex = groupIndex;
        return this;
    }

    public BodyBuilder setGroundFriction(float staticCOF, float kineticCOF,
            float staticTorqueCOF, float kineticTorqueCOF) {
        this.staticCOF = staticCOF;
        this.kineticCOF = kineticCOF;
        this.staticTorqueCOF = staticTorqueCOF;
        this.kineticTorqueCOF = kineticTorqueCOF;
        groundFriction = true;
        return this;
    }

    public Body build(World world) {
        Body body = world.createBody(bd);
        body.createFixture(fd);

        if (groundFriction) {
            TopDownFrictionJointDef jd = new TopDownFrictionJointDef();

            // TODO: calculate torque friction based on shape
            float normalForce = body.getMass() * ACCELERATION_GRAVITY;
            float staticFrictionForce = normalForce * staticCOF;
            float kineticFrictionForce = normalForce * kineticCOF;
            float staticFrictionTorque = normalForce * staticTorqueCOF;
            float kineticFrictionTorque = normalForce * kineticTorqueCOF;
            jd.initialize(body, staticFrictionForce, kineticFrictionForce, staticFrictionTorque,
                    kineticFrictionTorque);
            world.createJoint(jd);
        }

        return body;
    }

}
