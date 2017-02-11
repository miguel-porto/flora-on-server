package pt.floraon.occurrences;

/**
 * Enums and other constants used for occurrences.
 * Created by miguel on 05-02-2017.
 */
public final class OccurrenceConstants {
    /**
     * Whether the presence of this taxon in a location is wild or cultivated.
     */
    public enum OccurrenceNaturalization {WILD, CULTIVATED}
    public enum ConfidenceInIdentifiction {CERTAIN, ALMOST_SURE, DOUBTFUL}
    public enum CoverType {PERCENT_AREA, BRAUN_BLANQUET, TEXTUAL, AREA, OTHER_SCALE}
    public enum ValidationStatus {SPECIMEN_VERIFIED, ASSUMED_CORRECT, DOUBTFUL, PROBABLY_WRONG, WRONG, NOT_VALIDATED}

}
