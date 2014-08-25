package za.redbridge.simulator.object;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import sim.util.Double2D;


import static za.redbridge.simulator.Utils.toVec2;

/**
 * Builder API for creating Body objects with a single fixture.
 * Created by jamie on 2014/08/06.
 */
public class BodyBuilder {
    private BodyDef bd = new BodyDef();
    private FixtureDef fd = new FixtureDef();

    public BodyBuilder() {

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
        fd.density = (float) Math.PI * radius * radius / mass;
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
        fd.density = height * width / mass;
        return this;
    }

    public BodyBuilder setRectangular(double width, double height, double mass) {
        return setRectangular((float) width, (float) height, (float) mass);
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

    public Body build(World world) {
        Body body = world.createBody(bd);
        body.createFixture(fd);
        return body;
    }

}
