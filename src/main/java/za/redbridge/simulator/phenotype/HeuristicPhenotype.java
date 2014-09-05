package za.redbridge.simulator.phenotype;

import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.heuristics.CollisionAvoidanceHeuristic;
import za.redbridge.simulator.phenotype.heuristics.Heuristic;
import za.redbridge.simulator.phenotype.heuristics.PickupHeuristic;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.PickupSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by shsu on 2014/08/27.
 */
public class HeuristicPhenotype {

    protected final CollisionSensor collisionSensor;
    protected final PickupSensor pickupSensor;
    protected final Phenotype controllerPhenotype;
    protected final RobotObject attachedRobot;
    protected final SimConfig.Direction targetAreaPlacement;

    protected static final double P2 = Math.PI / 2;

    protected final PriorityBlockingQueue<Heuristic> heuristicList;


    public HeuristicPhenotype(Phenotype controllerPhenotype, RobotObject attachedRobot,
                              SimConfig.Direction targetAreaPlacement) {

        // TODO: Make configurable or decide on good defaults
        this.collisionSensor = new CollisionSensor();
        this.pickupSensor = new PickupSensor(attachedRobot.getRadius()/2, attachedRobot.getRadius(), 0f);
        this.controllerPhenotype = controllerPhenotype;
        this.attachedRobot = attachedRobot;
        this.targetAreaPlacement = targetAreaPlacement;

        collisionSensor.attach(attachedRobot);
        pickupSensor.attach(attachedRobot);

        heuristicList = new PriorityBlockingQueue<>();

        heuristicList.add(new CollisionAvoidanceHeuristic(collisionSensor, attachedRobot));
        heuristicList.add(new PickupHeuristic(pickupSensor, attachedRobot, heuristicList, targetAreaPlacement));
    }

    public CollisionSensor getCollisionSensor() { return collisionSensor; }
    public PickupSensor getPickupSensor() { return pickupSensor; }

    public HeuristicPhenotype clone() {
        return new HeuristicPhenotype(controllerPhenotype.clone(), attachedRobot,
                targetAreaPlacement);
    }

    public Double2D step(List<SensorReading> list) {

        Iterator<Heuristic> iterator = heuristicList.iterator();

        Double2D wheelDrives = null;

        while (wheelDrives == null && iterator.hasNext()) {
             wheelDrives = iterator.next().step(list);
        }

        if (wheelDrives == null) {
            wheelDrives = controllerPhenotype.step(list);
            System.out.println("Using controller.");
        }

        return wheelDrives;
    }

}
