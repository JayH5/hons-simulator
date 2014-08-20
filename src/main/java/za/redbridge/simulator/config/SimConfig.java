package za.redbridge.simulator.config;

import sim.util.Int2D;
import za.redbridge.simulator.ea.DefaultFitnessFunction;
import za.redbridge.simulator.ea.FitnessFunction;

public class SimConfig {

    public enum Direction {

        NORTH, SOUTH, EAST, WEST;
    }

    protected long seed;
    protected Int2D envSize;
    protected int numRobots;
    protected long maxIterations;

    protected Direction targetAreaPlacement;
    protected int targetAreaThickness;

    protected FitnessFunction fitnessFunction;

    protected boolean getValueFromArea;


    //default config
    public SimConfig() {
        this.seed = System.currentTimeMillis();
        this.envSize = new Int2D(102,102);
        this.numRobots = 15;
        this.targetAreaPlacement = Direction.SOUTH;
        this.targetAreaThickness = 20;
        this.fitnessFunction = new DefaultFitnessFunction();
        this.getValueFromArea = true;
        this.maxIterations = 10000;
    }

    public SimConfig(String filename) {
        throw new RuntimeException("TODO: Implement config reading from file");
    }

    public SimConfig(long seed, Int2D envSize, int numRobots, Direction targetAreaPlacement,
                     int targetAreaThickness, FitnessFunction fitness, boolean getValueFromArea, long maxIterations) {
        this.seed = seed;
        this.envSize = envSize;
        this.numRobots = numRobots;
        this.targetAreaPlacement = targetAreaPlacement;
        this.targetAreaThickness = targetAreaThickness;
        this.fitnessFunction = fitness;
        this.getValueFromArea = getValueFromArea;
        this.maxIterations = maxIterations;
    }

    public long getSeed() { return seed; }

    public void setSeed(long seed) { this.seed = seed; }

    public Int2D getEnvSize() {
        return envSize;
    }

    public int getNumRobots() {
        return numRobots;
    }

    public Direction getTargetAreaPlacement() { return targetAreaPlacement; }

    public int getTargetAreaThickness() { return targetAreaThickness; }

    public FitnessFunction getFitnessFunction() { return fitnessFunction; }

    public boolean getValueFromArea() { return getValueFromArea; }

}
