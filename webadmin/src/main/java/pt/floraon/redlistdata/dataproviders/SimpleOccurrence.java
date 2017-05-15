package pt.floraon.redlistdata.dataproviders;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geometry.CoordinateConversion;
import pt.floraon.geometry.UTMCoordinate;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.taxonomy.entities.CanonicalName;

/**
 * Created by miguel on 24-03-2017.
 */
public class SimpleOccurrence extends Inventory {
    /**
     * The data dataSource.
     */
    private final String dataSource;
    private newOBSERVED_IN occurrence;
    private final int id_reg;
    private final Integer id_ent;

    public String getDataSource() {
        return dataSource;
    }

    public int getId_reg() {
        return id_reg;
    }

    public Integer getId_ent() {
        return id_ent;
    }

    public newOBSERVED_IN getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(newOBSERVED_IN occurrence) {
        this.occurrence = occurrence;
    }

    public SimpleOccurrence(String dataSource, Inventory inv) {
        super(inv);
        this.dataSource = dataSource;
        this.id_reg = 0;
        this.id_ent = null;
        this.occurrence = inv._getTaxa()[0];
        if(this.getPrecision() == null) try {
            this.setPrecision("1");
        } catch (FloraOnException e) {
        }
    }

    public SimpleOccurrence(String dataSource, float latitude, float longitude, Integer year, Integer month, Integer day, String author
            , String genus, String species, String infrataxon, String pubNotes, int id_reg, Integer id_ent, String precision
            , OccurrenceConstants.ConfidenceInIdentifiction confidence, Constants.PhenologicalStates flowering) {
        this.dataSource = dataSource;
        this.id_reg = id_reg;
        this.id_ent = id_ent;

        this.occurrence = new newOBSERVED_IN();

        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setYear(year);
        this.setMonth(month);
        this.setDay(day);
        this._setObserverNames(new String[] {author});

        try {
            this.setPrecision(precision);
        } catch (FloraOnException e) {
            e.printStackTrace();
        }

        this.occurrence.setVerbTaxon(genus + " " + species + (infrataxon == null ? "" : " " + infrataxon));
        this.occurrence.setComment(pubNotes);
        this.occurrence.setConfidence(confidence);
        this.occurrence.setPhenoState(flowering);

/*
        getInventory().getPrecision()
        getOccurrence().getTypeOfEstimate()
*/

    }

    @Override
    public Float getLatitude() {
        if(this.getOccurrence().getObservationLatitude() == null)
            return super.getLatitude();
        else
            return this.getOccurrence().getObservationLatitude();
    }

    @Override
    public Float getLongitude() {
        if(this.getOccurrence().getObservationLongitude() == null)
            return super.getLongitude();
        else
            return this.getOccurrence().getObservationLongitude();
    }

}
