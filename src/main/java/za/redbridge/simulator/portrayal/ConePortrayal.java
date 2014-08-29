package za.redbridge.simulator.portrayal;

import java.awt.Paint;

/**
 * Created by jamie on 2014/08/07.
 */
public class ConePortrayal extends PolygonPortrayal {

    private static final double ARC_SEGMENT_RADIANS = Math.PI / 16;

    public ConePortrayal(double length, double theta, Paint paint) {
        super((int) Math.ceil(theta / ARC_SEGMENT_RADIANS) + 2, paint, true);

        int segments = nVertices - 2;
        double segmentRadians = theta / segments;
        double angle = -theta / 2;
        for (int i = 0; i <= segments; i++) {
            float x = (float) (length * Math.cos(angle));
            float y = (float) (length * Math.sin(angle));

            vertices[i + 1].set(x, y);

            angle += segmentRadians;
        }
    }

}
