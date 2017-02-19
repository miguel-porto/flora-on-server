package pt.floraon.redlistdata;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geometry.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 02-11-2016.
 */
public abstract class ExternalDataProvider implements Iterable<ExternalDataProvider.SimpleOccurrence> {
    protected List<SimpleOccurrence> occurrenceList;
    private IPolygonTheme clippingPolygon;
    private Integer minimumYear, maximumYear;

    /**
     * Returns how many occurrences
     * @return
     */
    public int size() {
        if(clippingPolygon == null && minimumYear == null && maximumYear == null)
            return occurrenceList.size();
        else {
            int count = 0;
            boolean enter;
            for(SimpleOccurrence so : occurrenceList) {
                enter = !(minimumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() < minimumYear);
                enter &= !(maximumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() > maximumYear);

                if(clippingPolygon != null) {
                    boolean tmp2 = false;
                    for(Map.Entry<String, Polygon> po : clippingPolygon) {
                        if(po.getValue().contains(new Point2D(so.getLongitude(), so.getLatitude()))) {
                            tmp2 = true;
                            break;
                        }
                    }
                    enter &= tmp2;
                }

                if(enter) count++;
            }
            return count;
        }
    }

    /**
     * Executes a query and updates the Iterable list of occurrences with the results.
     * @param query
     * @throws FloraOnException
     * @throws IOException
     */
    public abstract void executeOccurrenceQuery(Object query) throws FloraOnException, IOException;

    /**
     * Executes a query and returns arbitrary data about a taxon.
     * @param query
     * @throws FloraOnException
     * @throws IOException
     */
    public abstract Map<String, Object> executeInfoQuery(Object query) throws FloraOnException, IOException;

    public void exportKML(PrintWriter out) {
        final Kml kml = new Kml();
        Folder folder = kml.createAndSetFolder().withOpen(true).withName("Occurrences");
        for(ExternalDataProvider.SimpleOccurrence o : this) {
            Placemark pl = folder.createAndAddPlacemark();
            pl.withName(o.getGenus() + " " + o.getSpecies() + (o.getPrecision() == 1 ? " (100x100 m)" : (o.getPrecision() == 2 ? " (1x1 km)" : ""))).withDescription(o.getAuthor())
                    .createAndSetPoint().addToCoordinates(o.getLongitude(), o.getLatitude());
        }
        kml.marshal(out);
    }

    public class SimpleOccurrence {
        final float latitude, longitude;
        final Integer year, month, day;
        final String author, genus, species, infrataxon, notes;
        final int id_reg, id_ent, precision;

        public float getLatitude() {
            return latitude;
        }

        public float getLongitude() {
            return longitude;
        }

        public UTMCoordinate getUTMCoordinates() {
            return CoordinateConversion.LatLonToUtmWGS84(latitude, longitude, 0);
        }

        public Integer getYear() {
            return year;
        }

        public Integer getMonth() {
            return month;
        }

        public Integer getDay() {
            return day;
        }

        public String getAuthor() {
            return author;
        }

        public String getGenus() {
            return genus;
        }

        public String getSpecies() {
            return species;
        }

        public String getInfrataxon() {
            return infrataxon;
        }

        public String getNotes() {
            return notes;
        }

        public int getId_reg() {
            return id_reg;
        }

        public int getId_ent() {
            return id_ent;
        }

        public int getPrecision() {
            return precision;
        }

        public boolean getConfidence() {
            return confidence;
        }

        public Boolean getFlowering() {
            return flowering;
        }

        final boolean confidence;
        final Boolean flowering;

        public SimpleOccurrence(float latitude, float longitude, Integer year, Integer month, Integer day, String author
                , String genus, String species, String infrataxon, String notes, int id_reg, int id_ent, int precision
                , boolean confidence, Boolean flowering) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.year = year;
            this.month = month;
            this.day = day;
            this.author = author;
            this.genus = genus;
            this.species = species;
            this.infrataxon = infrataxon;
            this.notes = notes;
            this.id_reg = id_reg;
            this.id_ent = id_ent;
            this.precision = precision;
            this.confidence = confidence;
            this.flowering = flowering;
        }
    }

    @Override
    public Iterator<SimpleOccurrence> iterator() {
        if(clippingPolygon == null && minimumYear == null && maximumYear == null)
            return occurrenceList.iterator();
        else {
            List<SimpleOccurrence> out = new ArrayList<>();
            boolean enter;
            for(SimpleOccurrence so : occurrenceList) {
                enter = !(minimumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() < minimumYear);
                enter &= !(maximumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() > maximumYear);

                if(clippingPolygon != null) {
                    boolean tmp2 = false;
                    for(Map.Entry<String, Polygon> po : clippingPolygon) {
                        if(po.getValue().contains(new Point2D(so.getLongitude(), so.getLatitude()))) {
                            tmp2 = true;
                            break;
                        }
                    }
                    enter &= tmp2;
                }

                if(enter) out.add(so);
            }
            return out.iterator();
        }
    }

    /**
     * Sets the minimum year considered for including occurrences
     * @param minimumYear
     */
    public void setMinimumYear(Integer minimumYear) {
        this.minimumYear = minimumYear;
    }

    /**
     * Sets the maximum year considered for including occurrences
     * @param maximumYear
     */
    public void setMaximumYear(Integer maximumYear) {
        this.maximumYear = maximumYear;
    }

    /**
     * Sets a polygon theme to clip occurrences. May have any number of polygons.
     * @param theme
     */
    public void setClippingPolygon(IPolygonTheme theme) {
        clippingPolygon = theme;
    }
}
