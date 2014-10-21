package za.redbridge.simulator.config;

import org.yaml.snakeyaml.Yaml;
import za.redbridge.simulator.config.Config;
import za.redbridge.simulator.factories.ComplementFactory;
import za.redbridge.simulator.sensor.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/09/08.
 */

//TODO: serialise this
public class MorphologyConfig extends Config implements Serializable {

    private List<AgentSensor> sensorList;
    private final int numSensors;

    //total number of readings provided by this morphology
    private final int totalReadingSize;

    private Map<String,Object> yamlCache;


    public MorphologyConfig(List<AgentSensor> sensorList) {

        this.sensorList = sensorList;
        this.numSensors = sensorList.size();

        int readSize = 0;

        for (AgentSensor sensor: sensorList) {
            readSize += sensor.getReadingSize();
        }

        totalReadingSize = readSize;
    }

    public MorphologyConfig(String filepath) {

        sensorList = new ArrayList<>();
        int sensors = 0;
        int readingSize = 0;

        Yaml yaml = new Yaml();
        Map<String, Object> config = null;

        try (Reader reader =
                     Files.newBufferedReader(Paths.get(filepath), Charset.defaultCharset())) {
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
                System.out.println("Error: Number of sensors not found.");
                System.exit(-1);
            }
        }

        //TODO: make reading in sensor objects less hacktastic
        for (int i = 1; i <= sensors; i++) {

            String id = i + "s";

            String type = null;
            float bearing = 0, orientation = 0, fieldOfView = 0, range = 0;


            AgentSensor agentSensor = null;

            Map sensor = (Map) config.get(id);
            if (checkFieldPresent(sensor, id)) {

                Number bear = (Number) sensor.get("bearing");
                if (checkFieldPresent(bear, id + ":bearing")) {
                    bearing = bear.floatValue();
                }
                else {
                    System.out.println("No bearing found for sensor " + id);
                    System.exit(-1);
                }

                Number orient = (Number) sensor.get("orientation");
                if (checkFieldPresent(bear, id + ":orientation")) {
                    orientation = orient.floatValue();
                }
                else {
                    System.out.println("No orientation found for sensor " + id);
                    System.exit(-1);
                }

                Number fov = (Number) sensor.get("fieldOfView");
                if (checkFieldPresent(bear, id + ":fieldOfView")) {
                    fieldOfView = fov.floatValue();
                }
                else {
                    System.out.println("No field of view found for sensor " + id);
                    System.exit(-1);
                }

                Number ran = (Number) sensor.get("range");
                if (checkFieldPresent(bear, id + ":range")) {
                    range = ran.floatValue();
                }
                else {
                    System.out.println("No sensor range found for sensor " + id);
                    System.exit(-1);
                }

                type = (String) sensor.get("type");
                if (checkFieldPresent(type, id+":type")) {

                    try {
                        Class sensorType = Class.forName(type.trim());

                        Object o = sensorType.getConstructor(Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE)
                                .newInstance((float) Math.toRadians(bearing), (float) Math.toRadians(orientation),
                                        range, (float) Math.toRadians(fieldOfView));

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
                        System.exit(-1);
                    }
                    catch (InvocationTargetException inv) {
                        inv.getCause();
                        inv.printStackTrace();
                        System.exit(-1);
                    }
                    catch (InstantiationException ins) {
                        ins.printStackTrace();
                        System.exit(-1);
                    }
                    catch (IllegalAccessException ill) {
                        ill.printStackTrace();
                        System.exit(-1);
                    }
                    catch (ParseException p) {
                        p.printStackTrace();
                        System.exit(-1);
                    }

                }
                else {
                    System.out.println("Error: No sensor type found. ");
                    System.exit(-1);
                }
            }
            else {
                System.out.println("Error: " + i + " Sensor not found.");
                System.exit(-1);
            }

            sensorList.add(agentSensor);
        }

        numSensors = sensors;
        totalReadingSize = readingSize;

        System.out.println("read " + sensorList.size() + " sensors.");

        yamlCache = config;

    }

    public List<AgentSensor> getSensorList() { return sensorList; }

    public int getTotalReadingSize() { return totalReadingSize; }

    public int getNumSensors() { return numSensors; }

    public List<AgentSensor> getSensors() { return sensorList; }

    public void setSensors(List<AgentSensor> sensorList) { this.sensorList = sensorList; }

    @Override
    public int hashCode() { return Arrays.hashCode(getSensitivities()); }

    @Override
    public boolean equals(Object o) {

        if (o instanceof MorphologyConfig) {
            return Arrays.equals(getSensitivities(), ((MorphologyConfig) o).getSensitivities());
        }

        return false;
    }

    @Override
    public MorphologyConfig clone() {

        List<AgentSensor> newSensorList = new ArrayList<>();

        for (AgentSensor sensor: sensorList) {

            newSensorList.add(sensor.clone());
        }

        return new MorphologyConfig(newSensorList);
    }

    public int getNumAdjustableArrays() {

        int counter = 0;
        for (AgentSensor sensor : sensorList) {

            if (sensor instanceof LinearObjectProximityAgentSensor) {
                counter++;
            }
        }

        return counter;
    }

    public int getNumAdjustableSensitivities() {

        int counter = 0;
        for (AgentSensor sensor : sensorList) {

            if (sensor instanceof LinearObjectProximityAgentSensor) {
                counter+= 4;
            }
            else if (sensor instanceof AdjustableSensitivityAgentSensor) {
                counter+= 1;
            }
        }

        return counter;
    }

    public double[] getSensitivities() {

        double[] output = new double[getNumAdjustableSensitivities()];

        for (AgentSensor sensor : sensorList) {

            if (sensor instanceof LinearObjectProximityAgentSensor) {
                output = ((LinearObjectProximityAgentSensor) sensor).getGain();
            }

        }

        return output;
    }

    public void dumpMorphology(String path, String filename) {

        Map<String,Object> yamlDump = new HashMap<>();
        Map<String,Object> meta = new HashMap<>();
        meta.put("numSensors", new Integer(sensorList.size()));
        yamlDump.put("meta", meta);

        int sensorID = 1;
        for (AgentSensor sensor: sensorList) {

            Map<String,Object> sensorMap = sensor.getAdditionalConfigs();
            Map<String,Object> thisMap = new HashMap<>();

            thisMap.put("type", sensor.getClass().getName());
            thisMap.put("readingSize", sensor.getReadingSize());
            thisMap.put("bearing", Math.toDegrees(sensor.getBearing()));
            thisMap.put("orientation", Math.toDegrees(sensor.getOrientation()));
            thisMap.put("fieldOfView", Math.toDegrees(sensor.getFieldOfView()));
            thisMap.put("range", sensor.getRange());

            for (Map.Entry<String,Object> entry: sensorMap.entrySet()) {

                try {
                    Field field = sensor.getClass().getDeclaredField(entry.getKey());

                    try {

                        field.setAccessible(true);
                        thisMap.put(entry.getKey(), field.get(sensor));
                    }
                    catch(IllegalAccessException i) {
                        System.out.println("Illegal access of field " + entry.getKey());
                    }
                }
                catch (NoSuchFieldException n) {
                    //System.out.println("No such field: " + entry.getKey() + " " + sensorID);
                }
            }

            yamlDump.put(sensorID+"s", thisMap);
            sensorID++;
        }

        Yaml yaml = new Yaml();
        StringWriter stringWriter = new StringWriter();
        Path outputPath = Paths.get(path);

            try {
                Files.createDirectories(outputPath);
            }
            catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

        try (BufferedWriter fileWriter = Files.newBufferedWriter(outputPath.resolve(filename), StandardOpenOption.CREATE)){

            yaml.dump(yamlDump, stringWriter);
            fileWriter.write(stringWriter.toString());
        }
        catch (IOException e) {
            System.out.println("Error dumping morphology.");
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public static MorphologyConfig MorphologyFromGain (final MorphologyConfig template, double[] gain) {

        ArrayList<AgentSensor> newSensors = new ArrayList<>();

        for (AgentSensor sensor : template.getSensorList()) {
            AgentSensor clone = sensor.clone();

            if (clone instanceof LinearObjectProximityAgentSensor) {

                double[] gainCopy = new double[gain.length];
                System.arraycopy(gain, 0, gainCopy, 0, gain.length);

                ((LinearObjectProximityAgentSensor) clone).setGain(gainCopy);
            }
            newSensors.add(clone);
        }

        return new MorphologyConfig(newSensors);
    }

    public String parametersToString() {

        String output = "";

        for (AgentSensor sensor: sensorList) {

            if (sensor instanceof  AdjustableSensitivityAgentSensor) {
                output += ((AdjustableSensitivityAgentSensor) sensor).parametersToString() + "\n";
            }
        }

        return output;
    }

    public String getMorphologyID() {

        return parametersToString();
    }
}
