package pt.floraon.occurrences.entities;

import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.Objects;

/**
 * This represents an observation expunged from all fields except ID and confidence.
 * The purpose is to make species lists directly from observations.
 */
public class TaxEntObservation {
    public final OBSERVED_IN occurrence;
    public TaxEntObservation(OBSERVED_IN occurrence) {
        this.occurrence = occurrence;
    }

    public TaxEntObservation(TaxEnt taxEnt, OccurrenceConstants.ConfidenceInIdentifiction confidence) {
        this.occurrence = new OBSERVED_IN();
        this.occurrence.setTaxEntMatch(taxEnt.getID());
        this.occurrence.setTaxEnt(taxEnt);
        this.occurrence.setConfidence(confidence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxEntObservation that = (TaxEntObservation) o;
        boolean thisCertain = this.occurrence.getConfidence() == null
                || this.occurrence.getConfidence().equals(OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN)
                || this.occurrence.getConfidence().equals(OccurrenceConstants.ConfidenceInIdentifiction.NULL);
        boolean thatCertain = that.occurrence.getConfidence() == null
                || that.occurrence.getConfidence().equals(OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN)
                || that.occurrence.getConfidence().equals(OccurrenceConstants.ConfidenceInIdentifiction.NULL);

        return thisCertain == thatCertain &&
                Objects.equals(this.occurrence.getTaxEnt(), that.occurrence.getTaxEnt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.occurrence.getConfidence() == null
                || this.occurrence.getConfidence().equals(OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN)
                || this.occurrence.getConfidence().equals(OccurrenceConstants.ConfidenceInIdentifiction.NULL), this.occurrence.getTaxEnt());
    }
}
