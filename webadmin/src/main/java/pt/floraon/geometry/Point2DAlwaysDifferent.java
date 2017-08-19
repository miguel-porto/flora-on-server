package pt.floraon.geometry;

/**
 * A 2D point that is always different from other 2D points except if same instance.
 */
public class Point2DAlwaysDifferent extends Point2D {
    public Point2DAlwaysDifferent(double x, double y, Float latitude, Float longitude) {
        super(x, y, latitude, longitude);
    }

    public Point2DAlwaysDifferent(double x, double y) {
        super(x, y);
    }

    public Point2DAlwaysDifferent(UTMCoordinate coord, LatLongCoordinate geocoord) {
        super(coord, geocoord);
    }

    /**
     * Only returns true when points are the same instance.
     * @param other the other point
     * @return
     */
    @Override
    public boolean equals(Object other) {
        return other == this;
    }
}
