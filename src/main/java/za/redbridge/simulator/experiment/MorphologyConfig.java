package za.redbridge.simulator.experiment;

import org.yaml.snakeyaml.Yaml;
import za.redbridge.simulator.config.Config;
import za.redbridge.simulator.sensor.AgentSensor;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/09/08.
 */
public class MorphologyConfig extends Config {

    private List<AgentSensor> sensorList;
    private int numSensors;

    public MorphologyConfig(String filepath) throws ParseException {

        Yaml yaml = new Yaml();
        Map<String, Object> config = null;

        try (Reader reader = Files.newBufferedReader(Paths.get(filepath))) {
            config = (Map<String, Object>) yaml.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map meta = (Map) config.get("meta");
        if (checkFieldPresent(meta, "meta")) {
            Number noSensors = (Number) meta.get("numSensors");
            if (checkFieldPresent(noSensors, "meta:numSensors")) {
                noSensors = noSensors.intValue();
                numSensors = noSensors.intValue();
            }
            else {
                throw new ParseException("Error: Number of sensors not found.", 0);
            }
        }

        for (int i = 1; i <= numSensors; i++) {

            String id = "" + i;

            String type = null;
            float bearing, fieldOfView, range;

            Map sensor = (Map) config.get(id);
            if (checkFieldPresent(sensor, id)) {
                type = (String) sensor.get("type");
                if (checkFieldPresent(type, id+":type")) {

                }
                else {

                }
            }
            else {
                throw new ParseException("Error: Sensor not found.", i);
            }
        }

    }


}
