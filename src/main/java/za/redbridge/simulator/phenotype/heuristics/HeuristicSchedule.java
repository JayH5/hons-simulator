package za.redbridge.simulator.phenotype.heuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import sim.util.Double2D;
import za.redbridge.simulator.sensor.SensorReading;

/**
 * Created by jamie on 2014/09/10.
 */
public class HeuristicSchedule {
    private final PriorityQueue<Heuristic> schedule = new PriorityQueue<>();

    private final List<Heuristic> addList = new ArrayList<>();
    private final List<Heuristic> removeList = new ArrayList<>();

    public Double2D step(List<SensorReading> readings) {
        schedule.addAll(addList);
        addList.clear();

        schedule.removeAll(removeList);
        removeList.clear();

        Double2D wheelDrive = null;
        for (Heuristic heuristic : schedule) {
            wheelDrive = heuristic.step(readings);
            if (wheelDrive != null) {
                break;
            }
        }
        return wheelDrive;
    }

    public void addHeuristic(Heuristic heuristic) {
        if (!addList.contains(heuristic)) {
            addList.add(heuristic);
        }
    }

    public void removeHeuristic(Heuristic heuristic) {
        if (!removeList.contains(heuristic)) {
            removeList.add(heuristic);
        }
    }
}
