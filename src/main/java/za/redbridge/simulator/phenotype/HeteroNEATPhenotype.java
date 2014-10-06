package za.redbridge.simulator.phenotype;

import org.encog.ml.data.MLData;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.neat.NEATNetwork;
import sim.util.Double2D;
import za.redbridge.simulator.ea.hetero.CooperativeHeteroNEATNetwork;
import za.redbridge.simulator.sensor.AgentSensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by shsu on 2014/09/08.
 */
public class HeteroNEATPhenotype implements Phenotype {

    private final List<AgentSensor> sensors;
    private final CooperativeHeteroNEATNetwork controller;

    private final int totalInputCount;

    public HeteroNEATPhenotype(List<AgentSensor> sensors, CooperativeHeteroNEATNetwork controller, int totalInputCount) {

        this.sensors = sensors;
        this.controller = controller;
        this.totalInputCount = totalInputCount;
    }

    @Override
    public List<AgentSensor> getSensors () {
        return sensors;
    }

    @Override
    public Double2D step(List<List<Double>> list) {

        double[] inputs = new double[totalInputCount];
        int counter = 0;

        for (List<Double> reading: list) {
            for (Double value: reading) {

                inputs[counter] = value;
                counter++;
            }
        }

        BasicNeuralData inputData = new BasicNeuralData(inputs);
        MLData output = controller.getNetwork().compute(inputData);

        return new Double2D(output.getData(0)*2-1,output.getData(1)*2-1);
    }

    public HeteroNEATPhenotype clone() {

        List<AgentSensor> sensorList = new ArrayList<>();

        for (AgentSensor s: sensors) {
            sensorList.add(s.clone());
        }

        return new HeteroNEATPhenotype(sensorList, controller, totalInputCount);
    }

    public void configure(Map<String,Object> phenotypeConfigs) {}

}
