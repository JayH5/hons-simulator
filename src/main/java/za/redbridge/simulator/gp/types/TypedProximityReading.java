package za.redbridge.simulator.gp.types;

import za.redbridge.simulator.sensor.AgentSensor;

import java.util.Map;
import java.util.Optional;

/**
 * Created by xenos on 10/3/14.
 * A class to represent a typed reading; it contains a value as well as a type.
 */
public class TypedProximityReading extends ProximityReading {
    private final Optional<DetectedObject.Type> type;

    public TypedProximityReading(Map<DetectedObject.Type, Float> readingsMap, AgentSensor sensor){
        super(readingsMap.values().stream().filter(v -> v > 0.0).findFirst().orElse(0.0f), sensor);
        this.type = readingsMap.keySet().stream().filter(k -> readingsMap.get(k) > 0.0).findFirst();
    }

    public DetectedObject.Type getReadingType(){
        return type.orElse(DetectedObject.Type.NONE);
    }
}
