package pt.floraon.occurrences.entities;

import com.arangodb.velocypack.annotations.Expose;
import edu.emory.mathcs.backport.java.util.Collections;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.OccurrenceConstants;

/**
 * Represents an inventory which has only one taxon. The query engine is responsible for separating multiple taxa into
 * one inventory each.
 * This class can either be serialized from DB or created from values.
 */
public class Occurrence extends Inventory {
    /**
     * The data dataSource.
     */
    @Expose(serialize = false)
    private OBSERVED_IN occurrence;
    private int id_reg;
    private Integer id_ent;

    public int getId_reg() {
        return id_reg;
    }

    public Integer getId_ent() {
        return id_ent;
    }

    public OBSERVED_IN getOccurrence() {
        if(this.occurrence != null)
            return this.occurrence;
        if(this._getTaxa().length == 0)
            return null;
        else
            return this._getTaxa()[0];
    }

    public void setOccurrence(OBSERVED_IN occurrence) {
        this.setUnmatchedOccurrences(Collections.singletonList(occurrence));
//        this.occurrence = occurrence;
    }

    public Occurrence() {
        super();
    }

    public Occurrence(String dataSource, Inventory inv) {
        super(inv);
        this.setSource(dataSource);
        this.id_reg = 0;
        this.id_ent = null;
        OBSERVED_IN[] taxa = inv._getTaxa();
        if(taxa.length > 0)
            this.setUnmatchedOccurrences(Collections.singletonList(taxa[0]));

//        this.occurrence = inv._getTaxa()[0];
        if(this.getPrecision() == null) try {
            this.setPrecision("1");
        } catch (FloraOnException e) {
        }
    }

    public Occurrence(String dataSource, float latitude, float longitude, Integer year, Integer month, Integer day, String author
            , String genus, String species, String infrataxon, String pubNotes, int id_reg, Integer id_ent, String precision
            , OccurrenceConstants.ConfidenceInIdentifiction confidence, Constants.PhenologicalStates flowering, boolean escaped) {
        this.setSource(dataSource);
        this.id_reg = id_reg;
        this.id_ent = id_ent;

        OBSERVED_IN occurrence = new OBSERVED_IN();

        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setYear(year);
        this.setMonth(month);
        this.setDay(day);
        this._setObserverNames(new String[] {author});
        this.setMaintainer(author);

        try {
            this.setPrecision(precision);
        } catch (FloraOnException e) {
            e.printStackTrace();
        }

        this.setPubNotes(pubNotes);
        occurrence.setComment(pubNotes);
        occurrence.setVerbTaxon(genus + " " + species + (infrataxon == null ? "" : " " + infrataxon));
        occurrence.setConfidence(confidence);
        occurrence.setPhenoState(flowering);
        occurrence.setNaturalization(escaped ? OccurrenceConstants.OccurrenceNaturalization.ESCAPED : null);
        this.setUnmatchedOccurrences(Collections.singletonList(occurrence));

/*
        getInventory().getPrecision()
        getOccurrence().getTypeOfEstimate()
*/

    }

    @Override
    public Float _getLatitude() {
        if(this.getOccurrence().getObservationLatitude() == null)
            return super._getLatitude();
        else
            return this.getOccurrence().getObservationLatitude();
    }

    @Override
    public Float _getLongitude() {
        if(this.getOccurrence().getObservationLongitude() == null)
            return super._getLongitude();
        else
            return this.getOccurrence().getObservationLongitude();
    }

    public boolean hasCoordinate() {
        return !Constants.isNullOrNoData(this._getLatitude())
                && !Constants.isNullOrNoData(this._getLongitude());
    }
}
