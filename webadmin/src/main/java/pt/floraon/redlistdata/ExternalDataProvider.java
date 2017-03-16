package pt.floraon.redlistdata;

import pt.floraon.driver.FloraOnException;
import pt.floraon.geometry.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 02-11-2016.
 */
public abstract class ExternalDataProvider implements Iterable<ExternalDataProvider.SimpleOccurrence> {
    protected List<SimpleOccurrence> occurrenceList;

    public int size() {
        return this.occurrenceList.size();
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
        return occurrenceList.iterator();
    }

}
