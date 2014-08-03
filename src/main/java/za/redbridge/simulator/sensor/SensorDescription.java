package za.redbridge.simulator.sensor;

import sim.util.Double2D;

/**
 * Describes a sensor implementation. The actual sensor is implemented in the simulator.
 */
public final class SensorDescription {
    public enum Type {
        /** An infra-red proximity sensor */
        PROXIMITY,

        /** A simple low-pixel count CCD sensor */
        CAMERA,

        /** A detector for touching objects */
        PRESSURE
    }

    private final Type type;
    private final Double2D position;
    private final double orientation;
    private final double range;
    private final double fieldOfView;
    private final String tagFilter;

    public SensorDescription(Type type, Double2D position, double orientation, double range,
                double fieldOfView, String tagFilter) {
        this.type = type;
        this.position = position;
        this.orientation = orientation;
        this.range = range;
        this.fieldOfView = fieldOfView;
        this.tagFilter = tagFilter;
    }

    public Type getType() {
        return type;
    }

    public Double2D getPosition() {
        return position;
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

    public String getTagFilter() {
        return tagFilter;
    }

    public static class Builder {
        private Type type;
        private Double2D position;
        private double orientation;
        private double range;
        private double fieldOfView;
        private String tagFilter;

        public Builder() {
        }

        /**
         * Set the type of this sensor.
         * @param type the {@link SensorDescription.Type} of the sensor
         */
        public Builder setType(Type type) {
            if (type == null) {
                throw new IllegalArgumentException("Type may not be null");
            }

            this.type = type;
            return this;
        }

        /**
         * Set the position of the sensor relative to the center of the robot.
         * Default value: (0.0, 0.0)
         * @param position the position in MASON units
         */
        public Builder setPosition(Double2D position) {
            this.position = position;
            return this;
        }

        /**
         * Set the angle of the sensor relative to the agent's forward vector.
         * Default value: 0.0
         * @param orientation angle in radians (may be positive or negative)
         */
        public Builder setOrientation(double orientation) {
            final double twoPi = Math.PI * 2;
            while (orientation > twoPi) {
                orientation -= twoPi;
            }

            while (orientation < 0.0) {
                orientation += twoPi;
            }

            this.orientation = orientation;
            return this;
        }

        /**
         * Set the range of the sensor. This is the distance from its position the sensor can detect
         * objects.
         * Mandatory field
         * @param range the range in MASON units (must be > 0)
         */
        public Builder setRange(double range) {
            if (range <= 0.0) {
                throw new IllegalArgumentException("Range must be > 0");
            }

            this.range = range;
            return this;
        }

        /**
         * Sets the field of view of the
         * Mandatory field
         * @param fieldOfView the angle of field of view in radians. Must be in range (0, 2 * PI]
         */
        public Builder setFieldOfView(double fieldOfView) {
            if (fieldOfView <= 0.0 || fieldOfView > Math.PI * 2) {
                throw new IllegalArgumentException("Field of view must be > 0");
            }

            this.fieldOfView = fieldOfView;
            return this;
        }

        /**
         * Sets the object tag filter for this sensor. The sensor will only detect objects with this
         * tag. To detect all objects, set to null.
         * Default value: null
         * @param tagFilter the tag to filter against
         */
        public Builder setTagFilter(String tagFilter) {
            this.tagFilter = tagFilter;
            return this;
        }

        public SensorDescription build() {
            if (position == null) {
                position = new Double2D();
            }

            if (type == null || range == 0.0 || fieldOfView == 0.0) {
                throw new IllegalStateException(
                        "Not all mandatory fields set before calling build()");
            }

            return new SensorDescription(type, position, orientation, range, fieldOfView, tagFilter);
        }
    }
}
