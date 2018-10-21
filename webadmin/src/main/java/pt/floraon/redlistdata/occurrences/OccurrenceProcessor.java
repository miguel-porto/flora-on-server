package pt.floraon.redlistdata.occurrences;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.geometry.*;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.redlistdata.SVGMapExporter;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.occurrences.entities.Occurrence;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Processes a list of occurrences, computes a range of indices, and produces an SVG image with them.
 * Created by miguel on 01-12-2016.
 */
public class OccurrenceProcessor implements Iterable<Occurrence>, SVGMapExporter {
/*
    private final String[] colors = new String[] {"#ff0000", "#00ff00", "#0000ff", "#ffff00", "#ff00ff", "#00ffff"
            , "#770000", "#007700", "#000077", "#777700", "#770077", "#007777"
    };
*/
    private List<Cluster<Point2D>> clusters;
    private final Multimap<Point2D, Polygon> pointsInPolygons;   // for each occurrence lists the protected area polygons in which it falls
    private Stack<Point2D> convexHull;
    private Set<Square> squares;
    private IPolygonTheme protectedAreas;
    private Double EOO, realEOO, squareEOO, AOO;
    private int nQuads = 0;
    private long sizeOfSquare;
    private List<SimpleOccurrenceDataProvider> occurrences;
    private static final Polygon nullPolygon = new Polygon();
    private List<Occurrence> occurrenceList;
    private OccurrenceFilter occurrenceFilter;

    /**
     * Tests whether a given occurrence should be included in the iterator, given the provided filter.
     * @param so
     * @return
     */
    private boolean enter(Occurrence so) {
        return occurrenceFilter == null || occurrenceFilter.enter(so);
    }

    @Override
    public Iterator<Occurrence> iterator() {
        // if we've got a static occurrence list, just iterate over it. Otherwise, make new iterator from queries.
        if(this.occurrenceList == null)
            return new ExternalDataProviderIterator(occurrences);
        else
            return new OccurrenceListIterator(this.occurrenceList);
    }

    public class OccurrenceListIterator implements Iterator<Occurrence> {
        private List<Occurrence> occurrences;
        private Iterator<Occurrence> iterator;
        private Occurrence prevElement;

        OccurrenceListIterator(List<Occurrence> providers) {
            this.occurrences = providers;
            this.iterator = this.occurrences.iterator();
        }

        @Override
        public boolean hasNext() {
            if(prevElement != null) return true;

            while (this.iterator.hasNext()) {
                prevElement = this.iterator.next();
                if (enter(prevElement)) return true; else prevElement = null;
            }

            return false;
        }

        @Override
        public Occurrence next() {
            Occurrence so;
            if(this.prevElement != null) {
                so = this.prevElement;
                this.prevElement = null;
                return so;
            }

            while (this.iterator.hasNext()) {
                if (enter(so = this.iterator.next())) return so;
            }

            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public class ExternalDataProviderIterator implements Iterator<Occurrence> {
        private List<SimpleOccurrenceDataProvider> providers;
        private int curIteratorDataProvider = 0;
        private Iterator<Occurrence> curIterator;
        private Occurrence prevElement;

        ExternalDataProviderIterator(List<SimpleOccurrenceDataProvider> providers) {
            this.providers = providers;
        }

        @Override
        public boolean hasNext() {
            if(prevElement != null) return true;

            if(this.curIterator == null)
                this.curIterator = this.providers.get(0).iterator();

            do {
                while (this.curIterator.hasNext()) {
                    prevElement = this.curIterator.next();
                    if (enter(prevElement)) return true; else prevElement = null;
                }
                this.curIteratorDataProvider++;
                if(this.curIteratorDataProvider >= this.providers.size()) break;

                this.curIterator = this.providers.get(curIteratorDataProvider).iterator();
            } while(true);

            return false;
        }

        @Override
        public Occurrence next() {
            Occurrence so;
            if(this.prevElement != null) {
                so = this.prevElement;
                this.prevElement = null;
                return so;
            }
            if(this.curIterator == null)
                this.curIterator = this.providers.get(0).iterator();

            do {
                while (this.curIterator.hasNext()) {
                    if (enter(so = this.curIterator.next())) return so;
                }
                this.curIteratorDataProvider++;
                if(this.curIteratorDataProvider >= this.providers.size()) break;

                this.curIterator = this.providers.get(curIteratorDataProvider).iterator();
            } while(true);

            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns how many occurrences
     * @return
     */
    public int size() {
        int size = 0;
        if(this.occurrenceList == null) {
            for (SimpleOccurrenceDataProvider edp : this.occurrences) {
                for (Occurrence so : edp) {
                    if (enter(so)) size++;
                }
            }
        } else
            size = this.occurrenceList.size();
        return size;
    }

    public void exportKML(PrintWriter out) {
        final Kml kml = new Kml();
        Folder folder = kml.createAndSetFolder().withOpen(true).withName("Occurrences");
        for(Occurrence o : this) {
            if(o._getLatitude() == null || o._getLongitude() == null || !enter(o)) continue;
            Placemark pl = folder.createAndAddPlacemark();
            boolean hasEstimate = o.getOccurrence().getAbundance() != null
                    || (o.getOccurrence().getTypeOfEstimate() != null && o.getOccurrence().getTypeOfEstimate() != RedListEnums.TypeOfPopulationEstimate.NO_DATA);

            String name = o.getOccurrence().getVerbTaxon() +
                    (o.getOccurrence().getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL ? "?" : "") +
                    (o.getPrecision()._isImprecise() ? (" (" + o.getPrecision().toString() + ")") : "") +
                    (hasEstimate ? " JÁ CONTADO" : "");

            String desc = (o.getOccurrence().getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL ? "<b>Identificação duvidosa</b>" : "") +
                    "<table style=\"width:300px; font-family:sans; font-size:1.1em\">" +
                    "<tr><td style=\"background-color: #eee; width:0px\">Precisão</td><td>" + (o.getPrecision()._isImprecise() ? o.getPrecision().toString() : "máxima") + "</td></tr>" +
                    "<tr><td style=\"background-color: #eee; width:0px\">Data</td><td>" + o._getDate() + "</td></tr>" +
                    "<tr><td style=\"background-color: #eee; width:0px\">Local</td><td>"+ (o.getLocality() != null ? o.getLocality() : "") +
                        (o.getVerbLocality() != null ? "; " + o.getVerbLocality() : "") + "</td></tr>" +
                    "<tr><td style=\"background-color: #eee; width:0px\">Observadores</td><td>" + (o._getObserverNames().length > 0 ? o._getObserverNames()[0] : "<sem observador>") + "</td></tr>" +
                    "<tr><td style=\"background-color: #eee; width:0px\">Alt</td><td>" + (o.getElevation() == null ? "" : o.getElevation()) + "</td></tr>" +
                    "<tr><td style=\"background-color: #eee; width:0px\">Notas</td><td>" + (o.getOccurrence().getComment() != null ? o.getOccurrence().getComment() : "") +
                        (o.getOccurrence().getLabelData() != null ? o.getOccurrence().getLabelData() : "") + "</td></tr>" +
                    "<tr><td style=\"background-color: #eee; width:0px\">Estimativa</td><td>" +
                        (hasEstimate ? (o.getOccurrence().getTypeOfEstimate() + " = " + o.getOccurrence().getAbundance()) : "<não estimado>") + "</td></tr></table>";

            pl.withName(name).withDescription(desc)
                    .createAndSetPoint().addToCoordinates(o._getLongitude(), o._getLatitude());
        }
        kml.marshal(out);
    }

    public static OccurrenceProcessor iterableOf(List<SimpleOccurrenceDataProvider> occurrences) {
        return new OccurrenceProcessor(occurrences);
    }

    public static OccurrenceProcessor iterableOf(List<SimpleOccurrenceDataProvider> occurrences, OccurrenceFilter occurrenceFilter) {
        return new OccurrenceProcessor(occurrences, occurrenceFilter);
    }

    public static OccurrenceProcessor createFromOccurrences(List<Occurrence> occurrences, PolygonTheme protectedAreas, long sizeOfSquare
            , OccurrenceFilter occurrenceFilter) {

        OccurrenceProcessor out = new OccurrenceProcessor();

        out.occurrenceFilter = occurrenceFilter;
        out.protectedAreas = protectedAreas;
        out.sizeOfSquare = sizeOfSquare;
        out.occurrenceList = occurrences;

        if(sizeOfSquare > 0) out.computeMetrics();

        return out;
    }


    private OccurrenceProcessor() {
        this.pointsInPolygons = ArrayListMultimap.create();
    }

    /**
     * This constructor does nothing but providing an Iterator for all data providers merged. No calculations are done.
     * @param occurrences
     */
    private OccurrenceProcessor(List<SimpleOccurrenceDataProvider> occurrences) {
        this.occurrences = occurrences;
        this.pointsInPolygons = null;
        this.clusters = null;
    }

    private OccurrenceProcessor(List<SimpleOccurrenceDataProvider> occurrences, OccurrenceFilter occurrenceFilter) {
        this(occurrences, null, 0, occurrenceFilter);
    }
    /**
     * This constructor readily computes all indices from the data providers, for all records.
     * @param occurrences
     * @param protectedAreas
     * @param sizeOfSquare
     */
    public OccurrenceProcessor(List<SimpleOccurrenceDataProvider> occurrences, PolygonTheme protectedAreas, long sizeOfSquare
            , OccurrenceFilter occurrenceFilter) {// PolygonTheme clippingPolygon, Integer minimumYear, Integer maximumYear, boolean includeDoubtful) {
        this.occurrenceFilter = occurrenceFilter;
        this.protectedAreas = protectedAreas;
        this.sizeOfSquare = sizeOfSquare;
        this.pointsInPolygons = ArrayListMultimap.create();
        this.occurrences = occurrences;

        if(sizeOfSquare > 0) computeMetrics();
    }

    private void computeMetrics() {
        UTMCoordinate tmp;
        Point2D tmp1;
        Set<String> utmZones = new HashSet<>();

        if (this.size() == 0) {
            clusters = new ArrayList<>();
            return;
        }

        // process occurrences and at the same time assign each occurrence to the protected area it falls within
        for (Occurrence so : this) {
            tmp = so._getUTMCoordinates();
            if(tmp == null) continue;
            tmp1 = new Point2D(tmp, new LatLongCoordinate(so._getLatitude(), so._getLongitude()));
            utmZones.add(((Integer) tmp.getXZone()).toString() + java.lang.Character.toString(tmp.getYZone()));
            if(protectedAreas != null) {
                for (Map.Entry<String, pt.floraon.geometry.Polygon> e : protectedAreas) {
                    if (e.getValue().contains(new Point2D(so._getLongitude(), so._getLatitude()))) {
//                        System.out.println("Protected"+ tmp1.toString());
                        pointsInPolygons.put(tmp1, e.getValue());
                    }
                }
            }
            if (!pointsInPolygons.containsKey(tmp1))  // if the point does not fall in any polygon, add the point anyway
                pointsInPolygons.put(tmp1, nullPolygon);    // Multimap does not accept null values

        }

        // now calculate the number of UTM squares occupied
        squares = new HashSet<>();
        for (Point2D u : pointsInPolygons.keySet()) {
            squares.add(new Square(u, sizeOfSquare));
        }
//System.out.println(new Gson().toJson(squares));
        this.nQuads = squares.size();
        this.AOO = (this.nQuads * sizeOfSquare * sizeOfSquare) / 1000000d;
//        if (this.size() >= 3) {
        if(pointsInPolygons.keySet().size() >= 3) {
            // compute convex convexHull
            // TODO use a projection without zones
/*
            if (utmZones.size() > 1)
                request.setAttribute("warning", "EOO computation is inaccurate for data " +
                        "pointsUTM spreading more than one UTM zone.");
*/

            convexHull = (Stack<Point2D>) new GrahamScan(pointsInPolygons.keySet().toArray(new Point2D[0])).hull();
            convexHull.add(convexHull.get(0));
            double sum = 0.0;
            for (int i = 0; i < convexHull.size() - 1; i++) {
                sum = sum + (convexHull.get(i).x() * convexHull.get(i + 1).y()) - (convexHull.get(i).y() * convexHull.get(i + 1).x());
            }
            sum = 0.5 * sum;

            this.realEOO = sum / 1000000;
            if(this.realEOO < this.AOO)
                this.EOO = this.AOO;
            else
                this.EOO = this.realEOO;
        } else {
            EOO = (this.nQuads * sizeOfSquare * sizeOfSquare) / 1000000D;
            realEOO = null;
        }

        // compute convex hull of squares
        Iterator<Square> it = squares.iterator();
        List<Point2D> vertices = new ArrayList<>();
        while(it.hasNext()) {
            vertices.addAll(it.next().getVertices());
        }

        if(vertices.size() >= 3) {
            Stack<Point2D> tmpConvexHull = (Stack<Point2D>) new GrahamScan(vertices.toArray(new Point2D[0])).hull();
            tmpConvexHull.add(tmpConvexHull.get(0));
            double sum = 0.0;
            for (int i = 0; i < tmpConvexHull.size() - 1; i++) {
                sum = sum + (tmpConvexHull.get(i).x() * tmpConvexHull.get(i + 1).y()) - (tmpConvexHull.get(i).y() * tmpConvexHull.get(i + 1).x());
            }
            sum = 0.5 * sum;
            squareEOO = sum / 1000000;
        } else squareEOO = null;
        // now make a clustering to compute approximate number of locations
        DBSCANClusterer<Point2D> cls = new DBSCANClusterer<>(2500, 0);
        clusters = cls.cluster(pointsInPolygons.keySet());
    }

    @Override
    public void exportSVG(PrintWriter out, boolean showOccurrences, boolean showConvexhull, boolean showBaseMap, boolean standAlone, int border, boolean showShadow) {
        if(showBaseMap) {
            InputStream str = OccurrenceProcessor.class.getResourceAsStream(showShadow ? "../basemap.svg" : "../basemap-noshadow.svg");
            try {
                IOUtils.copy(str, out);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            out.print("<svg class=\"svgmap\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:lvf=\"http://flora-on.pt\" preserveAspectRatio=\"xMidYMin meet\" viewBox=\"440000 4090000 300000 597000\">" +
                    "<g transform=\"translate(0,8767000) scale(1,-1)\">");
        }
/*
        for (int i = 0; i < this.clusters.size(); i++) {
            Cluster<Point2D> cl = this.clusters.get(i);
            for(Point2D p : cl.getPoints()) {
                out.print("<circle cx=\"" + p.x() + "\" cy=\"" + p.y() + "\" r=\"3000\" style=\"fill:" + colors[i % colors.length] + "\" />");
            }
        }
*/

        if(protectedAreas != null) {
            // draw protected areas
            List<UTMCoordinate> tmp;
            for (Map.Entry<String, pt.floraon.geometry.Polygon> p : protectedAreas) {
                tmp = p.getValue().getUTMCoordinates();
                out.print("<path class=\"protectedarea\" d=\"M" + tmp.get(0).getX() + " " + tmp.get(0).getY());
                for (int i = 1; i < tmp.size(); i++) {
                    out.print("L" + tmp.get(i).getX() + " " + tmp.get(i).getY());
                }
                out.print("\"></path>");
            }
        }
        // draw convex hull
        if(showConvexhull && convexHull != null) {
            out.print("<path class=\"convexhull\" d=\"M" + (int) convexHull.get(0).x() + " " + (int) convexHull.get(0).y());
            for (int i = 1; i < convexHull.size(); i++) {
                out.print("L" + (int) convexHull.get(i).x() + " " + (int) convexHull.get(i).y());
            }
            out.print("\"></path>");
        }
//System.out.println("********** Process SVG");
        if(showOccurrences && this.squares != null) {
            // draw occurrence squares
            for (Square s : this.squares) {
                Rectangle2D s1 = s.getSquare();

                if(standAlone)
                    out.print("<rect class=\"utmsquare\" style=\"fill:#f55145; stroke:white; stroke-width:" + border
                            + "px\" vector-effect=\"non-scaling-stroke\" lvf:quad=\"" + s.getMGRS() + "\" x=\"" + s1.getMinX()
                            + "\" y=\"" + s1.getMinY() + "\" width=\"" + s1.getWidth() + "\" height=\"" + s1.getHeight() + "\"/>");
                else
                    out.print("<rect lvf:quad=\"" + s.getMGRS() + "\" x=\"" + s1.getMinX() + "\" y=\"" + s1.getMinY() + "\" width=\"" + s1.getWidth() + "\" height=\"" + s1.getHeight() + "\"/>");
            }
        }

        out.print("</g></svg>");
    }

    /**
     * Gets the official Extent of Occurrence, in km2, as per IUCN rules
     * @return
     */
    public Double getEOO() {
        return EOO;
    }

    /**
     * Gets the real EOO, computed with occurrence coordinates (only if >= 3)
     * @return
     */
    public Double getRealEOO() {
        return realEOO;
    }

    /**
     * Gets the EOO computed with the square vertices
     * @return
     */
    public Double getSquareEOO() {
        return squareEOO;
    }

    /**
     * Gets the number of squares where the species is present. The size of squares is given when instantiating the class.
     * @return
     */
    public int getNQuads() {
        return nQuads;
    }

    /**
     * Gets the area of occurrence, based on the squares whose size is given on instantiation.
     * @return
     */
    public Double getAOO() {
        return this.AOO;
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
    public Map<Polygon, Integer> getOccurrenceInProtectedAreas(Set<String> groupBy) {
        // FIXME: polygon hash should only include name and type!

        Map<Polygon, Integer> out = new HashMap<>();
        Set<Polygon> perCluster = null;
        List<Set<Polygon>> total = new ArrayList<>();

        for(Cluster<Point2D> cl : clusters) {
            perCluster = new HashSet<>();
            for(Point2D p : cl.getPoints()) {
                for(Polygon s : pointsInPolygons.get(p)) {
                    if(s.size() > 0) {   // falls within a PA polygon
                        s.setKeyFields(groupBy);    // TODO is this a good idea to change grouping, change de hash?!
                        perCluster.add(s);
                    }
                }
            }
            total.add(perCluster);
        }
        if(perCluster == null) return Collections.emptyMap();
        for(Set<Polygon> pc : total) {
            for(Polygon s : pc) {
                if(out.containsKey(s))
                    out.put(s, out.get(s) + 1);
                else
                    out.put(s, 1);
            }
        }

        // restore the original hash and equals
        for(Polygon p : pointsInPolygons.values())
            p.setKeyFields(null);

        return out;
    }

    /**
     * Gets the total number of locations which fall inside at least one protected area.
     * If at least one point in the cluster falls inside, then the location is counted.
     * @return
     */
    public int getNumberOfLocationsInsideProtectedAreas() {
        int count = 0;
        for(Cluster<Point2D> cl : clusters) {
            for(Point2D p : cl.getPoints()) {
                if(!pointsInPolygons.get(p).iterator().next().isNullPolygon()) {    // exists at least in one protected area
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    public int getNumberOfPointsOutsideProtectedAreas() {
        // FIXME this returns wrong values sometimes?
        int outside = 0;
        for(Map.Entry<Point2D, Collection<Polygon>> entry : pointsInPolygons.asMap().entrySet()) {
            if(entry.getValue().size() == 1 && entry.getValue().iterator().next().isNullPolygon()) outside++;
        }
        return outside;
    }

    /**
     * Gets an array of the areas of all locations. Area is computed as the area of the convex hull of each location.
     * @return
     */
    public Double[] getLocationAreas() {
        List<Double> areas = new ArrayList<>();
        Stack<Point2D> hull;
        for(Cluster<Point2D> cl : clusters) {
            switch(cl.getPoints().size()) {
                case 1:
                    areas.add(10000d);
                    break;

                case 2:
                    areas.add(20000d);
                    break;

                default:
                    hull = new GrahamScan(cl.getPoints().toArray(new Point2D[cl.getPoints().size()])).getHull();
                    areas.add(new Polygon(hull).area());
                    break;
            }
        }
        return areas.toArray(new Double[areas.size()]);
    }

}
