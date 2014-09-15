package za.redbridge.simulator.config;

import org.yaml.snakeyaml.Yaml;
import za.redbridge.simulator.config.Config;
import za.redbridge.simulator.sensor.AgentSensor;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/09/08.
 */
public class MorphologyConfig extends Config {

    private List<AgentSensor> sensorList;
    private final int numSensors;

    //total number of readings provided by this morphology
    private final int totalReadingSize;

    public MorphologyConfig(List<AgentSensor> sensorList) {

        this.sensorList = sensorList;
        this.numSensors = sensorList.size();

        int readSize = 0;

        for (AgentSensor sensor: sensorList) {
            readSize += sensor.getReadingSize();
        }

        totalReadingSize = readSize;
    }

    public MorphologyConfig(String filepath) throws ParseException {

        sensorList = new ArrayList<>();
        int sensors = 0;
        int totReadingSize = 0;

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
                sensors = noSensors.intValue();
            }
            else {
                throw new ParseException("Error: Number of sensors not found.", 0);
            }
        }

        //TODO: make reading in sensor objects less hacktastic
        for (int i = 1; i <= sensors; i++) {

            String id = i + "s";

            String type = null;
            float bearing, orientation, fieldOfView, range;
            int readingSize;

            AgentSensor agentSensor = null;

            Map sensor = (Map) config.get(id);
            if (checkFieldPresent(sensor, id)) {

                Integer reads = (Integer) sensor.get("readingSize");
                if (checkFieldPresent(reads, id + ":readingSize")) {
                    readingSize = reads;
                }
                else {
                    throw new ParseException("No reading size found for sensor " + id, i);
                }

                Number bear = (Number) sensor.get("bearing");
                if (checkFieldPresent(bear, id + ":bearing")) {
                    bearing = bear.floatValue();
                }
                else {
                    throw new ParseException("No bearing found for sensor " + id, i);
                }

                Number orient = (Number) sensor.get("orientation");
                if (checkFieldPresent(bear, id + ":orientation")) {
                    orientation = orient.floatValue();
                }
                else {
                    throw new ParseException("No orientation found for sensor " + id, i);
                }

                Number fov = (Number) sensor.get("fieldOfView");
                if (checkFieldPresent(bear, id + ":fieldOfView")) {
                    fieldOfView = fov.floatValue();
                }
                else {
                    throw new ParseException("No field of view found for sensor " + id, i);
                }

                Number ran = (Number) sensor.get("range");
                if (checkFieldPresent(bear, id + ":range")) {
                    range = ran.floatValue();
                }
                else {
                    throw new ParseException("No sensor range found for sensor " + id, i);
                }

                type = (String) sensor.get("type");
                if (checkFieldPresent(type, id+":type")) {

                    try {
                        Class sensorType = Class.forName(type.trim());

                        Object o = sensorType.getConstructor(Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE)
                                .newInstance((float) Math.toRadians(bearing), (float) Math.toRadians(orientation),
                                        (float) Math.toRadians(range), (float) Math.toRadians(fieldOfView));

                        if (!(o instanceof AgentSensor)) {
                            throw new InvalidClassException("Not Agent Sensor.");
                        }

                        agentSensor = (AgentSensor) o;
                        agentSensor.readAdditionalConfigs(sensor);

                        readingSize += agentSensor.getReadingSize();
                    }
                    catch (ClassNotFoundException c) {
                        System.out.println("AgentSensor Class not found for " + type);
                        c.printStackTrace();
                        System.exit(-1);
                    }
                    catch (InvalidClassException x) {
                        System.out.println("Invalid specified agent sensor class. " + type);
                        x.printStackTrace();
                        System.exit(-2);
                    }
                    catch (NoSuchMethodException n) {
                        n.printStackTrace();
                    }
                    catch (InvocationTargetException inv) {
                        inv.getCause();
                        inv.printStackTrace();
                    }
                    catch (InstantiationException ins) {
                        ins.printStackTrace();
                    }
                    catch (IllegalAccessException ill) {
                        ill.printStackTrace();
                    }

                }
                else {
                    throw new ParseException("Error: No sensor type found. ", i);
                }
            }
            else {
                throw new ParseException("Error: " + i + " Sensor not found.", i);
            }

            sensorList.add(agentSensor);
        }

        numSensors = sensors;
        totalReadingSize = totReadingSize;

        System.out.println("read " + sensorList.size() + " sensors.");
    }

    public List<AgentSensor> getSensorList() { return sensorList; }

    public int getTotalReadingSize() { return totalReadingSize; }

    public int getNumSensors() { return numSensors; }

    public List<AgentSensor> getSensors() { return sensorList; }

    public void setSensors(List<AgentSensor> sensorList) { this.sensorList = sensorList; }

    @Override
    public MorphologyConfig clone() {

        List<AgentSensor> newSensorList = new ArrayList<>();

        for (AgentSensor sensor: sensorList) {

            newSensorList.add(sensor.clone());
        }

        return new MorphologyConfig(newSensorList);
    }

}
