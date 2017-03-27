package pt.floraon.redlistdata.dataproviders;

import pt.floraon.geometry.CoordinateConversion;
import pt.floraon.geometry.UTMCoordinate;

/**
 * Created by miguel on 24-03-2017.
 */
public class SimpleOccurrence {
    /**
     * The data dataSource.
     */
    private final String dataSource;
    private final float latitude, longitude;
    private final Integer year, month, day;
    private final String author, genus, species, infrataxon, notes;
    private final int id_reg, precision;
    private final Integer id_ent;
    final boolean confidence;
    final Boolean flowering;

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

    public String getDataSource() {
        return dataSource;
    }

    public String getNotes() {
        return notes;
    }

    public int getId_reg() {
        return id_reg;
    }

    public Integer getId_ent() {
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

    public SimpleOccurrence(String dataSource, float latitude, float longitude, Integer year, Integer month, Integer day, String author
            , String genus, String species, String infrataxon, String notes, int id_reg, Integer id_ent, int precision
            , boolean confidence, Boolean flowering) {
        this.dataSource = dataSource;
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
