package pt.floraon.geometry.gridmaps;

import pt.floraon.geometry.CoordinateConversion;
import pt.floraon.geometry.Point2D;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Square implements ISquare {
    private long qx, qy;
    private Point2D point;
    private String MGRS;
    private long sizeOfSquare;

    public Square(Point2D coordinate, long sizeOfSquare) {
        this.point = coordinate;
        this.sizeOfSquare = sizeOfSquare;
        qx = (long) Math.floor(coordinate.x() / sizeOfSquare);
        qy = (long) Math.floor(coordinate.y() / sizeOfSquare);
    }

    @Override
    public Rectangle2D getSquare() {
        return new Rectangle2D.Double(qx * sizeOfSquare, qy * sizeOfSquare, sizeOfSquare, sizeOfSquare);
    }

    @Override
    public boolean hasColor() {
        return false;
    }

    @Override
    public String getColor() {
        return null;
    }

    @Override
    public void setColor(String color) {

    }

    public List<Point2D> getVertices() {
        List<Point2D> out = new ArrayList<>(4);
        out.add(new Point2D(qx * sizeOfSquare, qy * sizeOfSquare));
        out.add(new Point2D((qx + 1) * sizeOfSquare, qy * sizeOfSquare));
        out.add(new Point2D(qx * sizeOfSquare, (qy + 1) * sizeOfSquare));
        out.add(new Point2D((qx + 1) * sizeOfSquare, (qy + 1) * sizeOfSquare));
        return out;
    }

    @Override
    public String getMGRS() {
        if(MGRS == null) {
            MGRS = CoordinateConversion.LatLongToMGRS(this.point.getLatitude(), this.point.getLongitude(), sizeOfSquare);
        }
        return MGRS;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Square.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Square other = (Square) obj;
        if (this.qx != other.qx || this.qy != other.qy) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int x = Long.valueOf(qx).hashCode();
        int y = Long.valueOf(qy).hashCode();
        int tmp = (y + ((x+1)/2));
        return x + (tmp * tmp);
    }
}
