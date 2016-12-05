package pt.floraon.driver.results;

import pt.floraon.driver.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miguel on 11-11-2016.
 */
public class InferredStatus {
    protected Constants.NativeStatus nativeStatus;
    protected Constants.OccurrenceStatus occurrenceStatus;
    protected Constants.AbundanceLevel abundanceLevel;
    protected Constants.PlantIntroducedStatus introducedStatus;
    protected Constants.PlantNaturalizationDegree naturalizationDegree;
    /**
     * Status is assigned to a parent taxon. Should be read: it is not certain that it is this [sub-]taxon that exists in this territory,
     * but if it exists, then it is with this nativeStatus.
     */
    protected Boolean possibly;
    /**
     * Whether this taxon has been identified with certainty or not
     */
    protected Boolean uncertainOccurrence;
    /**
     * Wheter it is endemic in this territory (this can be inferred from the endemism in sub-territories)
     */
    protected Boolean endemic;
    /**
     * The long name of the territory this Status pertains to
     */
    protected String territoryName;

    public InferredStatus() {
    }

    public InferredStatus(TerritoryStatus ts, Boolean endemic) {
        this.nativeStatus = ts.existsIn.getNativeStatus();
        this.abundanceLevel = ts.existsIn.getAbundanceLevel();
        this.occurrenceStatus = ts.existsIn.getOccurrenceStatus();
        this.introducedStatus = ts.existsIn.getIntroducedStatus();
        this.naturalizationDegree = ts.existsIn.getNaturalizationDegree();
        this.territoryName = ts.territory.getName();
        this.uncertainOccurrence = ts.existsIn.isUncertainOccurrenceStatus();
        this.possibly = ts.edges.contains(Constants.RelTypes.PART_OF.toString())
                && ts.direction.get(ts.edges.indexOf(Constants.RelTypes.PART_OF.toString())).equals("OUTBOUND");
        this.endemic = endemic;
    }

    public Constants.NativeStatus getNativeStatus() {
        return this.nativeStatus;
    }

    public Constants.OccurrenceStatus getOccurrenceStatus() {
        return this.occurrenceStatus;
    }

    public Constants.AbundanceLevel getAbundanceLevel() {
        return this.abundanceLevel;
    }

    public Constants.PlantIntroducedStatus getIntroducedStatus() {
        return this.introducedStatus;
    }

    public Constants.PlantNaturalizationDegree getNaturalizationDegree() {
        return this.naturalizationDegree;
    }

    public boolean getPossibly() {
        return this.possibly;
    }

    public boolean getUncertainOccurrence() {
        return this.uncertainOccurrence;
    }

    public boolean isEndemic() {
        return this.endemic;
    }

    public boolean isRare() {
        return (this.abundanceLevel != null
                && this.abundanceLevel != Constants.AbundanceLevel.NOT_SPECIFIED
                && this.abundanceLevel != Constants.AbundanceLevel.VERY_COMMON
                && this.abundanceLevel != Constants.AbundanceLevel.COMMON);
    }

    public String getTerritoryName() {
        return this.territoryName;
    }

    /**
     * Gets a textual description of the native status of this taxon.
     *
     * @return
     */
    public String getStatusSummary() {
        List<String> qualifiers = new ArrayList<String>();
        if (this.getOccurrenceStatus() != Constants.OccurrenceStatus.PRESENT)
            qualifiers.add(this.getOccurrenceStatus().toString());
        if (this.getUncertainOccurrence()) qualifiers.add("uncertain");
        if (this.getPossibly()) qualifiers.add("if it exists");
        if (this.isEndemic()) qualifiers.add("ENDEMIC");
        if (this.getAbundanceLevel() != Constants.AbundanceLevel.NOT_SPECIFIED)
            qualifiers.add(this.getAbundanceLevel().toString());
        if (this.getIntroducedStatus() != Constants.PlantIntroducedStatus.NOT_SPECIFIED
                && this.getIntroducedStatus() != Constants.PlantIntroducedStatus.NOT_APPLICABLE)
            qualifiers.add(this.getIntroducedStatus().toString());
        if (this.getNaturalizationDegree() != Constants.PlantNaturalizationDegree.NOT_SPECIFIED
                && this.getNaturalizationDegree() != Constants.PlantNaturalizationDegree.NOT_APPLICABLE)
            qualifiers.add(this.getNaturalizationDegree().toString());

        return this.getNativeStatus().toString()
                + (qualifiers.size() > 0 ? " (" + Constants.implode(", ", qualifiers.toArray(new String[0])) + ")" : "");
    }
}
