package za.redbridge.simulator.object;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import sim.util.Double2D;
import sim.util.Int2D;
import za.redbridge.simulator.Simulation;
import za.redbridge.simulator.config.SimConfig;
import za.redbridge.simulator.ea.FitnessFunction;
import za.redbridge.simulator.portrayal.Portrayal;
import za.redbridge.simulator.portrayal.RectanglePortrayal;

import java.awt.*;
import java.util.HashSet;

import static za.redbridge.simulator.Utils.toVec2;

/**
 * Created by shsu on 2014/08/13.
 */
public class TargetAreaObject extends PhysicalObject {

        private int width, height;

        private FitnessFunction fitnessFunction;

        //total value of resources within this forage area
        private double totalObjectValue;

        //total fitness value for the agents in this simulation. unfortunately fitness is dead tied to forage area and
        //how much stuff is in there.
        private double totalFitness;

        //hash set so that object values only get added to forage area once
        private final HashSet<ResourceObject> containedObjects = new HashSet<>();

        //keeps track of what has been pushed into this place

        public TargetAreaObject(World world, Double2D pos, int width, int height, FitnessFunction fitnessFunction) {
            super(createPortrayal(width, height), createBody(world, pos, width, height));

            this.fitnessFunction = fitnessFunction;
            totalObjectValue = 0;
            totalFitness = 0;
            this.width = width;
            this.height = height;
        }

        protected static Portrayal createPortrayal(int width, int height) {
            Paint areaColour = new Color(31, 110, 11, 100);
            return new RectanglePortrayal(width, height, areaColour, true);
        }

        protected static Body createBody(World world, Double2D position, int width, int height) {
            BodyBuilder bb = new BodyBuilder();
            return bb.setBodyType(BodyType.STATIC)
                    .setPosition(toVec2(position))
                    .setRectangular(width, height)
                    .setSensor(true)
                    .build(world);
        }

        //these also update the overall fitness value
        public void setTotalObjectValue(double totalObjectValue) {
            this.totalObjectValue = totalObjectValue;
            totalFitness = fitnessFunction.calculateFitness(this.totalObjectValue);
        }

        public void incrementTotalObjectValue(double totalObjectValue) {
            this.totalObjectValue += totalObjectValue;
            totalFitness = fitnessFunction.calculateFitness(this.totalObjectValue);
        }

        public double getTotalFitness() { return totalFitness; }

        public void addResource(ResourceObject resourceObject) {
            if (!containedObjects.contains(resourceObject)) {
                containedObjects.add(resourceObject);
                incrementTotalObjectValue(resourceObject.getValue());
            }
        }

        public int getWidth() { return width; };
        public int getHeight() { return height; };

}
