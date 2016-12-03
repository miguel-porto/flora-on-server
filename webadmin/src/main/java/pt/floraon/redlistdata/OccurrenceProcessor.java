package pt.floraon.redlistdata;

import jline.internal.Log;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import pt.floraon.utmlatlong.GrahamScan;
import pt.floraon.utmlatlong.Point2D;
import pt.floraon.utmlatlong.UTMCoordinate;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Processes a list of occurrences to an SVG image
 * Created by miguel on 01-12-2016.
 */
public class OccurrenceProcessor {
    private final String[] colors = new String[] {"#ff0000", "#00ff00", "#0000ff", "#ffff00", "#ff00ff", "#00ffff"
            , "#770000", "#007700", "#000077", "#777700", "#770077", "#007777"
    };
    private final List<Point2D> points;
    private final List<Cluster<Point2D>> clusters;
    private Stack<Point2D> convexHull;
    private Set<Square> squares;
    private PolygonTheme protectedAreas;
    private Double EOO;
    private int nQuads;
    private long sizeOfSquare;
    private boolean showOccurrences = false;

    private class Square {
        private long qx, qy;

        public Square(UTMCoordinate coordinate) {
            qx = (long) Math.floor(coordinate.getX() / sizeOfSquare);
            qy = (long) Math.floor(coordinate.getY() / sizeOfSquare);
        }

        public Rectangle2D getSquare() {
            return new Rectangle2D.Double(qx * sizeOfSquare, qy * sizeOfSquare, sizeOfSquare, sizeOfSquare);
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

    public OccurrenceProcessor(ExternalDataProvider occurrences, long sizeOfSquare, boolean showOccurrences) {
        protectedAreas = new NamedPolygons(this.getClass().getResourceAsStream("SNAC.geojson"), "SITE_NAME");

        List<UTMCoordinate> utmCoords = new ArrayList<>(occurrences.size());
        this.points = new ArrayList<>();
        this.sizeOfSquare = sizeOfSquare;
        this.showOccurrences = showOccurrences;
        UTMCoordinate tmp;
        Point2D tmp1;
        Set<String> utmZones = new HashSet<>();

        for (ExternalDataProvider.SimpleOccurrence so : occurrences) {
            utmCoords.add(tmp = so.getUTMCoordinates());
            tmp1 = new Point2D(tmp);
            utmZones.add(((Integer) tmp.getXZone()).toString() + java.lang.Character.toString(tmp.getYZone()));
            for(Map.Entry<String, pt.floraon.utmlatlong.Polygon> e : protectedAreas) {
                if (e.getValue().contains(new Point2D(so.getLongitude(), so.getLatitude()))) {
                    tmp1.addTag(e.getKey());
                }
            }
            points.add(tmp1);
        }

        if (occurrences.size() >= 3) {
            // compute convex convexHull
            // first convert to UTM
            // TODO use a projection without zones
/*
            if (utmZones.size() > 1)
                request.setAttribute("warning", "EOO computation is inaccurate for data " +
                        "points spreading more than one UTM zone.");
*/

            convexHull = (Stack<Point2D>) new GrahamScan(points.toArray(new Point2D[0])).hull();
            convexHull.add(convexHull.get(0));
            double sum = 0.0;
            for (int i = 0; i < convexHull.size() - 1; i++) {
                sum = sum + (convexHull.get(i).x() * convexHull.get(i + 1).y()) - (convexHull.get(i).y() * convexHull.get(i + 1).x());
            }
            sum = 0.5 * sum;

            EOO = sum / 1000000;
        }

        // now calculate the number of UTM squares occupied
        squares = new HashSet<>();
        for (UTMCoordinate u : utmCoords) {
            squares.add(new Square(u));
        }

        this.nQuads = squares.size();

        // now make a clustering to compute approximate number of locations
        DBSCANClusterer<Point2D> cls = new DBSCANClusterer<>(2500, 0);
        clusters = cls.cluster(points);
        Log.info(clusters.size());
    }

    public void exportSVG(PrintWriter out) {
        InputStream str = this.getClass().getResourceAsStream("basemap.svg");
        try {
            IOUtils.copy(str, out);
        } catch (IOException e) {
            return;
        }
/*
        for (int i = 0; i < this.clusters.size(); i++) {
            Cluster<Point2D> cl = this.clusters.get(i);
            for(Point2D p : cl.getPoints()) {
                out.print("<circle cx=\"" + p.x() + "\" cy=\"" + p.y() + "\" r=\"3000\" style=\"fill:" + colors[i % colors.length] + "\" />");
            }
        }
*/

        // draw protected areas
        List<UTMCoordinate> tmp;
        for(Map.Entry<String, pt.floraon.utmlatlong.Polygon> p : protectedAreas) {
            tmp = p.getValue().getUTMCoordinates();
            out.print("<path class=\"protectedarea\" d=\"M" + tmp.get(0).getX() + " " + tmp.get(0).getY());
            for (int i = 1; i < tmp.size(); i++) {
                out.print("L" + tmp.get(i).getX() + " " + tmp.get(i).getY());
            }
            out.print("\"></path>");
        }

        // draw convex hull
        if(convexHull != null) {
            out.print("<path class=\"convexhull\" d=\"M" + (int) convexHull.get(0).x() + " " + (int) convexHull.get(0).y());
            for (int i = 1; i < convexHull.size(); i++) {
                out.print("L" + (int) convexHull.get(i).x() + " " + (int) convexHull.get(i).y());
            }
            out.print("\"></path>");
        }

        if(showOccurrences) {
            // draw occurrence squares
            for (Square s : this.squares) {
                Rectangle2D s1 = s.getSquare();
                out.print("<rect x=\"" + s1.getMinX() + "\" y=\"" + s1.getMinY() + "\" width=\"" + s1.getWidth() + "\" height=\"" + s1.getHeight() + "\"/>");
            }
        }

        out.print("</g></svg>");
    }

    /**
     * Gets the Extent of Occurrence, in km2
     * @return
     */
    public Double getEOO() {
        return EOO;
    }

    /**
     * Gets the number of squares where the species is present. The size of squares is given when instantiating the class.
     * @return
     */
    public int getNQuads() {
        return nQuads;
    }

    public List<Cluster<Point2D>> getClusters() {
        return this.clusters;
    }

    /**
     * Gets the number of locations where the species is present. A location is a cluster of occurrences, computed with
     * the given parameters.
     * @return
     */
    public int getNLocations() {
        return this.clusters.size();
    }

    /**
     * Gets the number of locations per protected area.
     * @return
     */
    public Map<String, Integer> getOccurrenceInProtectedAreas() {
        Map<String, Integer> out = new HashMap<>();
        Set<String> perCluster = null;
        List<Set<String>> total = new ArrayList<>();

        for(Cluster<Point2D> cl : clusters) {
            perCluster = new HashSet<>();
            for(Point2D p : cl.getPoints()) {
                for(String s : p.getTags()) {
                    perCluster.add(s);
                }
            }
            total.add(perCluster);
        }
        if(perCluster == null) return out;
        for(Set<String> pc : total) {
            for(String s : pc) {
                if(out.containsKey(s))
                    out.put(s, out.get(s) + 1);
                else
                    out.put(s, 1);
            }
        }
        return out;
    }

    /**
     * Gets the total number of locations which fall inside at least one protected area.
     * @return
     */
    public int getNumberOfLocationsInsideProtectedAreas() {
        int count = 0;
        for(Cluster<Point2D> cl : clusters) {
            for(Point2D p : cl.getPoints()) {
                if(p.getTags().size() > 0) {    // exists at least in one protected area
                    count++;
                    break;
                }
            }
        }
        return count;
    }
}
