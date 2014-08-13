package za.redbridge.simulator.object;

import org.jbox2d.common.Mat22;
import org.jbox2d.common.Mat33;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import sim.engine.SimState;
import sim.util.Double2D;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.portrayal.CirclePortrayal;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.sensor.Sensor;
import za.redbridge.simulator.sensor.SensorReading;


import static za.redbridge.simulator.Utils.toVec2;

/**
 * Object that represents a finished agent in the environment, including controller and all physical attributes.
 *
 * All RobotObjects are round with a fixed radius.
 *
 * Created by jamie on 2014/07/23.
 */
public class RobotObject extends PhysicalObject {

    private static final double WHEEL_POWER = 0.005; //how much force a wheel exerts when driven at full power
    private final Phenotype phenotype;

    public RobotObject(World world, Double2D position, double radius, double mass, Paint paint,
                       Phenotype phenotype) {
        super(createPortrayal(radius, paint), createBody(world, position, radius, mass));
        this.phenotype = phenotype;
    }

    protected static Portrayal createPortrayal(double radius, Paint paint) {
        return new CirclePortrayal(radius, paint, true);
    }

    protected static Body createBody(World world, Double2D position, double radius, double mass) {
        BodyBuilder bb = new BodyBuilder();
        return bb.setBodyType(BodyType.DYNAMIC)
                .setPosition(toVec2(position))
                .setCircular((float) radius)
                .setDensity((float) (mass / (Math.PI * radius * radius)))
                .setFriction(0.7f)
                .setRestitution(1.0f)
                .build(world);
    }

    @Override
    public void step(SimState sim) {
        super.step(sim);

        /*Sensor sensor = phenotype.getSensors().get(0);
        SensorReading reading = sensor.sense(((Simulation)sim).getEnvironment(), this);
        double dist = reading.getValues().get(0);

        this.getShape().setPaint(new Color((int) (dist*255),0,0));
        setAngularVelocity(getAngularVelocity() * 0.9);
        setVelocity(new Double2D(getVelocity().x * 1.005, getVelocity().y * 1.005));*/

        Simulation simulation = (Simulation) sim;

        List<Sensor> sensors = phenotype.getSensors();
        List<SensorReading> readings = new ArrayList<SensorReading>(sensors.size());
        for(Sensor sensor : sensors) {
            //readings.add(sensor.sense(((Simulation)sim).getEnvironment(), this));
        }
        //Double2D wheelForces = phenotype.step(readings);
        Double2D wheelForces = new Double2D(1.0, 0.2);
        if(Math.abs(wheelForces.x) > 1.0 || Math.abs(wheelForces.y) > 1.0) {
            throw new RuntimeException("Invalid force applied: " + wheelForces);
        }
        //rotation matrix
        Mat22 rotM = Mat22.createRotationalTransform(getBody().getAngle());

        Vec2 wheel1Force = rotM.mul(new Vec2(0, (float)(wheelForces.x * WHEEL_POWER)));
        Vec2 wheel2Force = rotM.mul(new Vec2(0, (float)(wheelForces.y * WHEEL_POWER)));

        double radius = ((CirclePortrayal)getPortrayal()).getRadius();
        Vec2 position = getBody().getPosition();
        Vec2 wheel1Offset = rotM.mul(new Vec2((float)(0.75*radius),0));
        Vec2 wheel2Offset = rotM.mul(new Vec2((float)(-0.75*radius),0));

        Vec2 wheel1Position = position.add(wheel1Offset);
        Vec2 wheel2Position = position.add(wheel2Offset);

        getBody().applyForce(wheel1Force, wheel1Position);
        getBody().applyForce(wheel2Force, wheel2Position);
    }

    /*private static class RobotShape extends Rectangle {

        final Phenotype phenotype;

        public RobotShape(double radius, Paint paint, Phenotype phenotype) {
            super(radius * 2, radius * 2, paint);
            this.phenotype = phenotype;
        }

        @Override
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            super.draw(object, graphics, info);

            Angle orientation = getOrientation();
            // Draw all the sensors
            for (Sensor sensor : phenotype.getSensors()) {
                sensor.draw(object, graphics, info, orientation);
            }
        }
    }*/
}
