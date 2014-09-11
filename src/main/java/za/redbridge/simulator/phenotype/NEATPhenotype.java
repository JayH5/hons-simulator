package za.redbridge.simulator.phenotype;

import org.encog.ml.data.MLData;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.neat.NEATNetwork;
import org.encog.util.simple.EncogUtility;
import sim.util.Double2D;
import za.redbridge.simulator.sensor.AgentSensor;
import za.redbridge.simulator.sensor.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/09/08.
 */
public class NEATPhenotype implements Phenotype {

    private final List<AgentSensor> sensors;
    private final NEATNetwork controller;

    private final int totalInputCount;

    public NEATPhenotype(List<AgentSensor> sensors, NEATNetwork controller, int totalInputCount) {

        this.sensors = sensors;
        this.controller = controller;
        this.totalInputCount = totalInputCount;
    }

    @Override
    public List<AgentSensor> getSensors () {
        return sensors;
    }

    @Override
    public Double2D step(List<SensorReading> list) {



        double[] inputs = new double[totalInputCount];
        int counter = 0;

        for (SensorReading reading: list) {
            for (Double value: reading.getValues()) {

                inputs[counter] = value;
                counter++;
            }
        }

        BasicNeuralData inputData = new BasicNeuralData(inputs);
        MLData output = controller.compute(inputData);

        return new Double2D(output.getData(0),output.getData(1));
    }

    public NEATPhenotype clone() {

        List<AgentSensor> sensorList = new ArrayList<>();

        for (AgentSensor s: sensorList) {
            sensorList.add(s.clone());
        }

        //todo: clone the controller???

        return new NEATPhenotype(sensorList, controller, totalInputCount);
    }

    public NEATNetwork getController() { return controller; }

    public void configure(Map<String,Object> phenotypeConfigs) {}

}
