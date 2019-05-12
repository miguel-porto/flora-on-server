package pt.floraon.geometry;

import com.arangodb.velocypack.annotations.Expose;
import jline.internal.Log;

import java.util.*;

/**
 * Created by miguel on 01-12-2016.
 */
public class Polygon {
    private int N;        // number of points in the polygon
    private Point2D[] points;    // the points, setting points[0] = points[N]
    @Expose(serialize = false, deserialize = false)
    private transient List<UTMCoordinate> UTMCoordinates; // for caching purposes
    /**
     * The data fields
     */
    private Map<String, Object> properties = new HashMap<>();
    /**
     * The fields used for assessing polygon uniqueness. If this set is empty, all fields and coordinates are used.
     * If not empty, only these fields are used (and not coordinates).
     */
    private Set<String> keyFields;

    public Polygon() {
        N = 0;
        points = new Point2D[4];
    }

    /**
     * Polygon must be open!
     * @param points
     */
    public Polygon(Stack<Point2D> points) {
        N = 0;
        this.points = new Point2D[points.size()];
        for(Point2D p : points)
            this.add(p);

    }

    // double size of array
    private void resize() {
        Point2D[] temp = new Point2D[2*N+1];
        for (int i = 0; i <= N; i++) temp[i] = points[i];
        points = temp;
    }

    // return size
    public int size() { return N; }

    public boolean isNullPolygon() {
        return N == 0;
    }

    // add point p to end of polygon
    public void add(Point2D p) {
        if (N >= points.length - 1) resize();   // resize array if needed
        points[N++] = p;                        // add point
        points[N] = points[0];                       // close polygon

//        UTMCoordinate ut = CoordinateConversion.LatLonToUtmWGS84(p.y, p.x, 0);
//        System.out.println(ut.getX()+", "+ut.getY());
    }

    // return the perimeter
    public double perimeter() {
        double sum = 0.0;
        for (int i = 0; i < N; i++)
            sum = sum + points[i].distanceTo(points[i+1]);
        return sum;
    }

    // return signed area of polygon
    public double area() {
        double sum = 0.0;
        for (int i = 0; i < N; i++) {
            sum = sum + (points[i].x * points[i+1].y) - (points[i].y * points[i+1].x);
        }
        return 0.5 * sum;
    }

    // does this Polygon contain the point p?
    // if p is on boundary then 0 or 1 is returned, and p is in exactly one point of every partition of plane
    // Reference: http://exaflop.org/docs/cgafaq/cga2.html
    public boolean contains2(Point2D p) {
        int crossings = 0;
        for (int i = 0; i < N; i++) {
            int j = i + 1;
            boolean cond1 = (points[i].y <= p.y) && (p.y < points[j].y);
            boolean cond2 = (points[j].y <= p.y) && (p.y < points[i].y);
            if (cond1 || cond2) {
                // need to cast to double
                if (p.x < (points[j].x - points[i].x) * (p.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)
                    crossings++;
            }
        }
        return crossings % 2 == 1;
    }

    // does this Polygon contain the point p?
    // Reference: http://softsurfer.com/Archive/algorithm_0103/algorithm_0103.htm
    public boolean contains(Point2D p) {
        int winding = 0;
        for (int i = 0; i < N; i++) {
            int ccw = Point2D.ccw(points[i], points[i+1], p);
            if (points[i+1].y >  p.y && p.y >= points[i].y)  // upward crossing
                if (ccw == +1) winding++;
            if (points[i+1].y <= p.y && p.y <  points[i].y)  // downward crossing
                if (ccw == -1) winding--;
        }
        return winding != 0;
    }


    // return string representation of this point
    @Override
    public String toString() {
        if (N == 0) return "[ ]";
        String s = "";
        s = s + "[ ";
        for (int i = 0; i <= N; i++)
            s = s + points[i] + " ";
        s = s + "]";
        return s;
    }

    public Iterator<Point2D> getCoordinates() {
        return new Point2DIterator();
    }

    public class Point2DIterator implements Iterator<Point2D> {
        private int i = 0;
        @Override
        public boolean hasNext() {
            return this.i <= N;
        }

        @Override
        public Point2D next() {
            this.i++;
            return points[i - 1];
        }

        @Override
        public void remove() {

        }
    }

    public List<UTMCoordinate> getUTMCoordinates() {
        if(this.UTMCoordinates == null) {
            UTMCoordinates = new ArrayList<>();
            for (int i = 0; i <= N; i++) {
                UTMCoordinates.add(CoordinateConversion.LatLonToUtmWGS84(points[i].y, points[i].x, 0));
            }
        }
        return UTMCoordinates;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String toSVGPathString() {
        return this.toSVGPathString(1);
    }

    public String toSVGPathString(int divisor) {
        StringBuilder sb1 = new StringBuilder();
        for (int i = 0; i <= N; i++) {
            sb1.append(i == 0 ? "M" : "L")
                    .append(String.format("%.0f", points[i].x() / divisor)).append(" ")
                    .append(String.format("%.0f", points[i].y() / divisor));
        }
        return sb1.toString();
    }

    public String toSVGPathStringUTM(int divisor) {
        this.getUTMCoordinates();
        StringBuilder sb1 = new StringBuilder();
        boolean first = true;
        for(UTMCoordinate coo : this.UTMCoordinates) {
            sb1.append(first ? "M" : "L")
                    .append(String.format("%d", coo.getX() / divisor)).append(" ")
                    .append(String.format("%d", coo.getY() / divisor));
            first = false;
        }
        return sb1.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Polygon polygon = (Polygon) o;

        if(keyFields != null && keyFields.size() > 0) {
            Iterator<String> it = keyFields.iterator();
            boolean eq = true;
            String a;
            while(it.hasNext()) {
                a = it.next();
                eq &= properties.get(a).equals(polygon.properties.get(a));
            }
            return eq;
        } else {
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(points, polygon.points)) return false;
            return properties != null ? properties.equals(polygon.properties) : polygon.properties == null;
        }
    }

    @Override
    public int hashCode() {
        if(keyFields != null && keyFields.size() > 0) {
            switch(keyFields.size()) {
                case 1:
                    return properties.get(keyFields.iterator().next()).hashCode();
                case 2:
                    Iterator<String> it = keyFields.iterator();
                    String a = it.next();
                    String b = it.next();
                    return Objects.hash(properties.get(a), properties.get(b));
                default:
                    Log.warn("Not implemented");
                    return 0;   // FIXME
            }
        } else {
            int result = Arrays.hashCode(points);
            result = 31 * result + (properties != null ? properties.hashCode() : 0);
            return result;
        }
    }

    public void setKeyFields(Set<String> keyFields) {
        this.keyFields = keyFields;
    }
}

