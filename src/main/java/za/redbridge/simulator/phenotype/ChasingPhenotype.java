package za.redbridge.simulator.phenotype;

import java.util.List;
import java.util.Map;

import sim.util.Double2D;
import za.redbridge.simulator.khepera.KheperaIIIPhenotype;
import za.redbridge.simulator.khepera.UltrasonicSensor;
import za.redbridge.simulator.physics.FilterConstants;
import za.redbridge.simulator.sensor.AgentSensor;

public class ChasingPhenotype extends KheperaIIIPhenotype {
    private static final int COOLDOWN = 10;

    private int cooldownCounter = 0;
    private Double2D lastMove = null;

    private static final Configuration CONFIG = new Configuration();
    static {
        CONFIG.enableUltrasonicSensor0Degrees = true;
        CONFIG.enableUltrasonicSensors40Degrees = true;
    }

    public ChasingPhenotype() {
        super(CONFIG);
    }

    @Override
    public Double2D step(List<List<Double>> list) {
        Double2D left = new Double2D(0.5,1.0);
        Double2D forward = new Double2D(1.0,1.0);
        Double2D right = new Double2D(1.0,0.5);
        Double2D random = new Double2D((float)Math.random()*2f - 1f, (float)Math.random()*2f - 1f);

        if(cooldownCounter > 0) {
            cooldownCounter--;
            return lastMove;
        }else {
            cooldownCounter = COOLDOWN;
        }

        double leftReading = list.get(0).get(0);
        double forwardReading = list.get(1).get(0);
        double rightReading = list.get(2).get(0);
        double max = Math.max(leftReading, Math.max(forwardReading, rightReading));
        if(max < 0.0001){
            lastMove = random;
            return random;
        }else if(leftReading == max) {
            lastMove = left;
            return left;
        }else if(rightReading == max) {
            lastMove = right;
            return right;
        }else {
            lastMove = forward;
            return forward;
        }
    }

    @Override
    public ChasingPhenotype clone() {
        return new ChasingPhenotype();
    }

    @Override
    protected AgentSensor createUltrasonicSensor(float bearing, float orientation) {
        return new UltrasonicSensor(bearing, orientation) {
            @Override
            protected int getFilterMaskBits() {
                return FilterConstants.CategoryBits.RESOURCE;
            }
        };
    }

    @Override
    public void configure(Map<String,Object> phenotypeConfigs) {}
}
