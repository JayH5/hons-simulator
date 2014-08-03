package za.redbridge.simulator.sensor;

import java.util.ArrayList;
import java.util.List;

import sim.field.continuous.Continuous2D;
import sim.physics2D.physicalObject.PhysicalObject2D;
import sim.util.Bag;
import sim.util.Double2D;
import za.redbridge.simulator.object.RobotObject;
import za.redbridge.simulator.object.Taggable;


import static za.redbridge.simulator.Utils.angleBetweenPoints;

/**
 * All sensors are simulated within this class.
 * Created by jamie on 2014/08/03.
 */
public class SensorEngine {

    private final Continuous2D environment;

    public SensorEngine(Continuous2D environment) {
        this.environment = environment;
    }

    public List<SensorReading> sense(RobotObject robot, List<SensorDescription> sensors) {
        List<SensorReading> readings = new ArrayList<>(sensors.size());

        for (SensorDescription sensor : sensors) {
            List<PhysicalObject2D> eligibleObjects = findObjectsWithinSensorField(robot, sensor);

            SensorReading reading = senseObjects(sensor, eligibleObjects);

            readings.add(reading);
        }

        return readings;
    }

    private List<PhysicalObject2D> findObjectsWithinSensorField(RobotObject robot,
            SensorDescription sensor) {
        List<PhysicalObject2D> objects = new ArrayList<>();

        // Work out effective position of sensor
        Double2D robotPosition = robot.getPosition();
        final Double2D effectiveSensorPosition = new Double2D(
                robotPosition.x + sensor.getPosition().x,
                robotPosition.y + sensor.getPosition().y
        );
        final double effectiveSensorRotation =
                robot.getOrientation().radians + sensor.getOrientation();

        // Find objects within the sensor range
        Bag neighbours =
                environment.getNeighborsWithinDistance(effectiveSensorPosition, sensor.getRange());


        // Filter which of these objects match the sensor criteria
        for (Object neighbour : neighbours) {
            if (neighbour == robot || !(neighbour instanceof PhysicalObject2D)) {
                continue; // Not much we can do
            }

            PhysicalObject2D obj = (PhysicalObject2D) neighbour;

            // If there is a tag filter for this sensor apply it
            String tagFilter = sensor.getTagFilter();
            if (tagFilter != null) {
                // If object not taggable or tag does not match, skip it
                if (!(obj instanceof Taggable) || !tagFilter.equals(((Taggable) obj).getTag())) {
                    continue;
                }
            }

            // Finally, check if object is within the sensor's field of view
            Double2D objPosition = obj.getPosition();
            double angle = angleBetweenPoints(effectiveSensorPosition, objPosition);
            if (Math.abs(effectiveSensorRotation - angle) <= sensor.getFieldOfView() / 2.0) {
                objects.add(obj);
            }
        }

        return objects;
    }

    private SensorReading senseObjects(SensorDescription sensor,
            List<PhysicalObject2D> objects) {

        switch (sensor.getType()) {
            case PROXIMITY:
                return proximityReading(objects);
            case CAMERA:
                return cameraReading(objects);
            case PRESSURE:
                return pressureReading(objects);
        }
        return null;
    }

    private SensorReading proximityReading(List<PhysicalObject2D> objects) {
        // TODO
        return null;
    }

    private SensorReading cameraReading(List<PhysicalObject2D> objects) {
        // TODO
        return null;
    }

    private SensorReading pressureReading(List<PhysicalObject2D> objects) {
        // TODO
        return null;
    }

}
