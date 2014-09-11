package za.redbridge.simulator.phenotype;

import java.util.List;

import sim.util.Double2D;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.phenotype.heuristics.CollisionAvoidanceHeuristic;
import za.redbridge.simulator.phenotype.heuristics.HeuristicSchedule;
import za.redbridge.simulator.phenotype.heuristics.PickupHeuristic;
import za.redbridge.simulator.sensor.ClosestObjectSensor;
import za.redbridge.simulator.sensor.CollisionSensor;
import za.redbridge.simulator.sensor.PickupSensor;
import za.redbridge.simulator.sensor.SensorReading;

/**
 * Created by shsu on 2014/08/27.
 */
public class HeuristicPhenotype {

    protected final CollisionSensor collisionSensor;
    protected final PickupSensor pickupSensor;
    protected final Phenotype controllerPhenotype;
    protected final RobotObject attachedRobot;
    protected final SimConfig.Direction targetAreaPlacement;

    protected final HeuristicSchedule schedule;

    public HeuristicPhenotype(Phenotype controllerPhenotype, RobotObject attachedRobot,
            SimConfig.Direction targetAreaPlacement) {

        // TODO: Make configurable or decide on good defaults
        this.collisionSensor = new CollisionSensor();
        this.pickupSensor = new PickupSensor(0.5f);
        this.controllerPhenotype = controllerPhenotype;
        this.attachedRobot = attachedRobot;
        this.targetAreaPlacement = targetAreaPlacement;

        collisionSensor.attach(attachedRobot);
        pickupSensor.attach(attachedRobot);

        schedule = new HeuristicSchedule();

        schedule.addHeuristic(
                new PickupHeuristic(schedule, pickupSensor, attachedRobot, targetAreaPlacement));
        schedule.addHeuristic(
                new CollisionAvoidanceHeuristic(schedule, collisionSensor, attachedRobot));
    }

    public ClosestObjectSensor getCollisionSensor() {
        return collisionSensor;
    }

    public PickupSensor getPickupSensor() {
        return pickupSensor;
    }

    @Override
    public HeuristicPhenotype clone() {
        return new HeuristicPhenotype(controllerPhenotype.clone(), attachedRobot,
                targetAreaPlacement);
    }

    public Double2D step(List<SensorReading> list) {
        Double2D wheelDrives = schedule.step(list);

        if (wheelDrives == null) {
            wheelDrives = controllerPhenotype.step(list);
            if(wheelDrives.x == Double.NaN || wheelDrives.y == Double.NaN){
                throw new RuntimeException("Controller gave NaN wheelDrives!");
            }
        }

        return wheelDrives;
    }

}
