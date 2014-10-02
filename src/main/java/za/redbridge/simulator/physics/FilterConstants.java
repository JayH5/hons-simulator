package za.redbridge.simulator.physics;

/**
 * Created by jamie on 2014/10/02.
 */
public final class FilterConstants {

    private FilterConstants() {
    }

    public static final class CategoryBits {
        private CategoryBits() {
        }

        public static final int DEFAULT = 1; // JBox2D default
        public static final int RESOURCE = 1 << 2;
        public static final int ROBOT = 1 << 3;
        public static final int WALL = 1 << 4;
        public static final int TARGET_AREA = 1 << 5;
        public static final int HEURISTIC_SENSOR = 1 << 6;
        public static final int AGENT_SENSOR = 1 << 7;
        public static final int TARGET_AREA_SENSOR = 1 << 8;
    }

    public static final class GroupIndexes {
        private GroupIndexes() {
        }

        /** Setting a negative group number for sensors mean that all sensors never collide. */
        public static final int SENSOR = -1;
    }
}
