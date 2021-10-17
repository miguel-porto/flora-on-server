package pt.floraon.occurrences;

import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.HashSet;
import java.util.Set;

public class OBSERVED_IN_summary {
    public int count = 1;
    public OccurrenceConstants.ConfidenceInIdentifiction maxConfidence = OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN;
    final private Set<TaxEnt> taxEnts = new HashSet<>();

    public OBSERVED_IN_summary(TaxEnt taxEnt, int count, OccurrenceConstants.ConfidenceInIdentifiction confidenceInIdentifiction) {
        this.count = count;
        taxEnts.add(taxEnt);
        this.maxConfidence = confidenceInIdentifiction;
        if(this.maxConfidence == null || this.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.NULL)
            this.maxConfidence = OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN;
    }

    public void mergeWith(OBSERVED_IN_summary that) {
        this.count += that.count;
        this.taxEnts.addAll(that.taxEnts);
        if(this.maxConfidence == null || this.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.NULL
                || this.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN
                || that.maxConfidence == null || that.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.NULL
                || that.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN) {
            this.maxConfidence = OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN;
            return;
        }

        if(this.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.ALMOST_SURE
                || that.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.ALMOST_SURE) {
            this.maxConfidence = OccurrenceConstants.ConfidenceInIdentifiction.ALMOST_SURE;
            return;
        }
        if(this.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL
                || that.maxConfidence == OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL) {
            this.maxConfidence = OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL;
            return;
        }
    }

    public String getNames() {
        Set<String> tmp = new HashSet<>();
        for(TaxEnt te : this.taxEnts) {
            try {
                tmp.add(te.getTaxonName().getCanonicalName(true));
            } catch (TaxonomyException e) {
                tmp.add(te.getNameWithAnnotationOnly(false));
            }
        }
        return StringUtils.implode(", ", tmp.toArray());
    }
}
