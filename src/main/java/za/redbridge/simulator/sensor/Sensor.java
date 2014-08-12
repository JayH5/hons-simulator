package za.redbridge.simulator.sensor;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;

import sim.util.Double2D;
import za.redbridge.simulator.object.PhysicalObject;


import static za.redbridge.simulator.Utils.HALF_PI;
import static za.redbridge.simulator.Utils.normaliseAngle;

/**
 * Describes a sensor implementation. The actual sensor is implemented in the simulator.
 */
public abstract class Sensor {

    private static final Paint PAINT = new Color(100, 100, 100, 100);
    private static final boolean DRAW_SHAPE = true;

    protected final double bearing;
    protected final double orientation;
    protected final double range;
    protected final double fieldOfView;

    private final double fovGradient;

    // Cache the position of the sensor relative to the robot to save recalculation.
    // If the sensor is reattached to a robot of a different radius then this position will be
    // recalculated.
    private Double2D cachedLocalPosition;
    private double cachedRobotRadius;

    //private Cone shape;

    public Sensor(double bearing, double orientation, double range, double fieldOfView) {
        this.bearing = bearing;
        this.orientation = orientation;
        this.range = range;
        this.fieldOfView = fieldOfView;

        fovGradient = Math.tan(HALF_PI - fieldOfView / 2);
    }

    /*private void initShape() {
        shape = new Cone(cachedLocalPosition, range, fieldOfView / 2);
    }

    public void draw(Object object, Graphics2D graphics, DrawInfo2D info, Angle orientation) {
        if (shape != null) {
            shape.setOrientation(orientation);
            shape.draw(object, graphics, info);
        }
    }

    public double getBearing() {
        return bearing;
    }

    public double getOrientation() {
        return orientation;
    }

    public double getRange() {
        return range;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public Cone getShape() {
        return shape;
    }

    public final SensorReading sense(Continuous2D environment, RobotObject robot) {
        // Get all information about the robot once, since most of these calls are not free
        Double2D robotPosition = robot.getPosition();
        double robotOrientation = robot.getOrientation().radians;
        double robotRadius = robot.getShape().getMaxXDistanceFromCenter();//((Circle) robot.getShape()).getRadius();

        // Calculate the sensor's effective position and orientation in the scene
        Double2D globalPosition = calculatePosition(robotPosition, robotOrientation, robotRadius);
        double globalOrientation = calculateOrientation(robotOrientation);

        // Find objects within the field of the sensor
        List<SensedObject> sensedObjects =
                findObjectsWithinField(globalPosition, globalOrientation, robot, environment);

        return provideReading(sensedObjects);
    }

    /** Calculates the global position of the sensor in the scene. */
    /*private Double2D calculatePosition(Double2D robotPosition, double robotOrientation,
            double robotRadius) {
        if (cachedLocalPosition == null || cachedRobotRadius != robotRadius) {
            double x = robotRadius * Math.cos(bearing);
            double y = robotRadius * Math.sin(bearing);
            cachedLocalPosition = new Double2D(x, y);
            cachedRobotRadius = robotRadius;

            if (DRAW_SHAPE) {
                initShape();
            }
        }

        return cachedLocalPosition.rotate(robotOrientation).add(robotPosition);
    }*/

    /** Calculates the global orientation of the sensor in the scene. */
    private double calculateOrientation(double robotOrientation) {
        return normaliseAngle(robotOrientation + bearing + orientation);
    }

    /** Find objects within range and return those within the FoV. */
    /*private List<SensedObject> findObjectsWithinField(Double2D globalPosition,
            double globalOrientation, RobotObject robot, Continuous2D environment) {
        List<SensedObject> objects = new ArrayList<>();

        // Iterate through neighbours, finding those within field of view
        Bag neighbours = environment.getNeighborsWithinDistance(globalPosition, range);
        for (Object neighbour : neighbours) {
            if (neighbour == robot || !(neighbour instanceof PhysicalObject2D)) {
                continue; // Not much we can do
            }

            PhysicalObject2D obj = (PhysicalObject2D) neighbour;
            SensedObject sensedObject = senseObject(obj, globalPosition, globalOrientation);
            if (sensedObject != null) {
                objects.add(sensedObject);
            }
        }

        // Sort objects so closest objects first in list
        Collections.sort(objects);

        return objects;
    }*/

    /**
     * Determines whether an object lies within the field of the sensor and if so where in the field
     * the object exists.
     * @param object the object to check
     * @param sensorPosition the global position of the sensor in the scene
     * @param sensorOrientation the global orientation of the sensor in the scene
     * @return a {@link SensedObject} reading if the object is in the field, else null
     */
    /*private SensedObject senseObject(PhysicalObject2D object, Double2D sensorPosition,
            double sensorOrientation) {
        Double2D objectPosition = object.getPosition();

        // Find the object's position in the "sensor space"
        // i.e. position as if sensor at origin, sensor forward vec is at 90 degrees
        double rotation = Math.PI / 2.0 - sensorOrientation;
        Double2D objectRelativePosition =
                objectPosition.subtract(sensorPosition).rotate(rotation);

        // Use an approximation of the distance from the sensor - just the y distance in the sensor
        // space.
        double objectDistance = objectRelativePosition.y;

        // If the object is behind us, then it can't be sensed
        if (objectDistance < 0.0) {
            return null;
        }

        // Boundaries of field of view obey equation y = mx + c
        // Where: m = (+/-) fovGradient, c = 0
        // We can get the symmetrical span across the x-axis of the field of view of the sensor for
        // a distance y from the sensor.
        double x1 = objectDistance / fovGradient;
        double x0 = -x1;

        // Calculate relative start and end points of the object
        Shape shape = object.getShape();
        double objectX0, objectX1;
        if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            objectX0 = objectRelativePosition.x - circle.getRadius();
            objectX1 = objectRelativePosition.x + circle.getRadius();
        } else if (shape instanceof Rectangle) {
            //Rectangle rectangle = (Rectangle) shape;
            //double width = rectangle.getWidth();
            //double height = rectangle.getHeight();
            // TODO: Maths is hard but this is doable
            double width = object.getShape().getMaxXDistanceFromCenter();
            objectX0 = objectRelativePosition.x - width;
            objectX1 = objectRelativePosition.x + width;
        } else {
            return null; // Don't know about this shape
        }

        // Check if object within field at all
        if (objectX1 < x0 || objectX0 > x1) {
            return null;
        }

        // Clamp span to field of sensor
        double spanStart = objectX0 > x0 ? objectX0 : x0;
        double spanEnd = objectX1 < x1 ? objectX1 : x1;

        return new SensedObject(object, objectDistance, spanStart, spanEnd);
    }*/

    /**
     * Converts a list of objects that have been determined to fall within the sensor's range into
     * an actual {@link SensorReading} instance.
     * @param objects the objects in the sensor's field, sorted by distance
     * @return the reading of the objects produced by the sensor
     */
    protected abstract SensorReading provideReading(List<SensedObject> objects);

    /**
     * Container class for intermediary sensor readings - contains the object to be sensed and
     * information about its location.
     */
    protected static class SensedObject implements Comparable<SensedObject> {
        private final PhysicalObject object;
        private final double spanStart;
        private final double spanEnd;
        private final double distance;

        public SensedObject(PhysicalObject object, double dist, double spanStart, double spanEnd) {
            this.object = object;
            this.distance = dist;
            this.spanStart = spanStart;
            this.spanEnd = spanEnd;
        }

        /** Get the object that has been sensed */
        public PhysicalObject getObject() {
            return object;
        }

        /** Get the estimated distance to the object. */
        public double getDistance() {
            return distance;
        }

        /** Get the start of the object's coverage of the field of view */
        public double getSpanStart() {
            return spanStart;
        }

        /** Get the end of the object's coverage of the field of view */
        public double getSpanEnd() {
            return spanEnd;
        }

        @Override
        public int compareTo(SensedObject o) {
            return Double.compare(distance, o.distance);
        }
    }

    /*private static class Cone extends Polygon {

        final Double2D position;
        final double length;
        final double theta;

        Angle orientation;

        private Cone(Double2D position, double length, double theta) {
            this.position = position;
            this.length = length;
            this.theta = theta;

            this.paint = PAINT;

            // Cool class design, bro.
            initVertices();
            initEdges();
            initNormals();
        }

        @Override
        public void initVertices() {
            double x0 = position.x;
            double y0 = position.y;

            double xDiff = length * Math.cos(theta);
            double yDiff = length * Math.sin(theta);

            double x1 = x0 + xDiff;
            double y1 = y0 + yDiff;
            double x2 = x0 + xDiff;
            double y2 = y0 - yDiff;

            double[][] vertices = {
                    { x0, x1, x2 },
                    { y0, y1, y2 },
                    { 1, 1, 1 }
            };
            this.vertices = new DenseMatrix(vertices);
        }

        @Override
        public void initEdges() {
            final double[][] vertices = this.vertices.vals;

            double[][] edges = new double[3][3];
            for (int i = 0; i < 3; i++) {
                edges[i] = new double[3];
            }

            Double2D edge = new Double2D(vertices[0][1], vertices[1][1])
                    .subtract(new Double2D(vertices[0][0], vertices[1][0]))
                    .normalize();

            edges[0][0] = edge.x;
            edges[1][0] = edge.y;
            edges[2][0] = 1;

            edge = new Double2D(vertices[0][2], vertices[1][2])
                    .subtract(new Double2D(vertices[0][1], vertices[1][1]))
                    .normalize();

            edges[0][1] = edge.x;
            edges[1][1] = edge.y;
            edges[2][1] = 1;

            edge = new Double2D(vertices[0][0], vertices[1][0])
                    .subtract(new Double2D(vertices[0][2], vertices[1][2]))
                    .normalize();

            edges[0][2] = edge.x;
            edges[1][2] = edge.y;
            edges[2][2] = 1;

            this.edges = new DenseMatrix(edges);
        }

        @Override
        public void initNormals() {
            final double[][] edges = this.edges.vals;

            double[][] normals = new double[3][3];
            for (int i = 0; i < 3; i++) {
                normals[i] = new double[3];
            }

            normals[0][0] = -edges[1][0];
            normals[1][0] = edges[0][0];
            normals[2][0] = 1;

            normals[0][1] = -edges[1][1];
            normals[1][1] = edges[0][1];
            normals[2][1] = 1;

            normals[0][2] = -edges[1][2];
            normals[1][2] = edges[0][2];
            normals[2][2] = 1;

            this.normals = new DenseMatrix(normals);
        }

        void setOrientation(Angle orientation) {
            this.orientation = orientation;
        }

        @Override
        protected Angle getOrientation() {
           return orientation;
        }

        @Override
        public double getMassMomentOfInertia(double v) {
            return 0;
        }

        @Override
        public void calcMaxDistances(boolean b) {
            // NO-OP
        }
    }*/

}
