package pt.floraon.occurrences;

import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.fields.flavours.*;
import pt.floraon.redlistdata.RedListEnums;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enums and other constants used for occurrences.
 * Created by miguel on 05-02-2017.
 */
public final class OccurrenceConstants {
    static public Map<String, IOccurrenceFlavour> occurrenceManagerFlavours = new LinkedHashMap<>();
    static {
        occurrenceManagerFlavours.put("simple", new SimpleFlavour());
        occurrenceManagerFlavours.put("redlist", new RedListFlavour());
        occurrenceManagerFlavours.put("herbarium", new HerbariumFlavour());
        occurrenceManagerFlavours.put("management", new ManagementFlavour());
        occurrenceManagerFlavours.put("inventory", new InventoryFlavour());
    }
    /**
     * Whether the presence of this taxon in a location is wild or cultivated.
     */
    public enum OccurrenceNaturalization {WILD, CULTIVATED, ESCAPED}
    public enum ConfidenceInIdentifiction implements RedListEnums.LabelledEnum {
        CERTAIN("Certain")
        , ALMOST_SURE("Almost sure")
        , DOUBTFUL("Doubtful")
        , NULL("");

        private String label;
        private static Map<String, ConfidenceInIdentifiction> acronymMap = new HashMap<>();
        static {
            acronymMap.put("c", OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN);
            acronymMap.put("a", OccurrenceConstants.ConfidenceInIdentifiction.ALMOST_SURE);
            acronymMap.put("d", OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL);
            acronymMap.put("", OccurrenceConstants.ConfidenceInIdentifiction.NULL);
        }

        ConfidenceInIdentifiction(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        static public ConfidenceInIdentifiction getValueFromAcronym(String acronym) throws IllegalArgumentException {
            ConfidenceInIdentifiction value1;
            acronym = acronym.toLowerCase();
            if(ConfidenceInIdentifiction.acronymMap.containsKey(acronym))
                value1 = ConfidenceInIdentifiction.acronymMap.get(acronym);
            else {
                try {
                    value1 = ConfidenceInIdentifiction.valueOf(acronym.toUpperCase());
                } catch(IllegalArgumentException e) {
                    for(ConfidenceInIdentifiction ce : ConfidenceInIdentifiction.values()) {
                        if(ce.toString().toLowerCase().startsWith(acronym))
                            return ce;
                    }
                    throw new IllegalArgumentException(acronym + " not understood, possible options: "
                            + StringUtils.implode(", ", ConfidenceInIdentifiction.acronymMap.keySet().toArray(new String[0])));
                }
            }

            return value1;
        }

    }
    public enum CoverType {BRAUN_BLANQUET, TEXTUAL, AREA, OTHER_SCALE}
    public enum ValidationStatus{SPECIMEN_VERIFIED, ASSUMED_CORRECT, DOUBTFUL, PROBABLY_WRONG, WRONG, NOT_VALIDATED}
    public enum PresenceStatus implements RedListEnums.LabelledEnum {
        ASSUMED_PRESENT("")
        , DESTROYED("Destroyed")
        , PROBABLY_MISIDENTIFIED("Misidentified?")
        , ESCAPED("Escaped?")
        , INTRODUCED("Introduced")
        , OTHER_REASON("Other exclusion reason")
        , WRONG_GEORREF("Wrong georref");

        private String label;
        private static Map<String, PresenceStatus> acronymMap = new HashMap<>();
        static {
            acronymMap.put("d", DESTROYED);
            acronymMap.put("m", PROBABLY_MISIDENTIFIED);
            acronymMap.put("g", WRONG_GEORREF);
            acronymMap.put("w", WRONG_GEORREF);
            acronymMap.put("e", ESCAPED);
            acronymMap.put("i", INTRODUCED);
            acronymMap.put("o", OTHER_REASON);
            acronymMap.put("", ASSUMED_PRESENT);
        }

        PresenceStatus(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        static public PresenceStatus getValueFromAcronym(String acronym) throws IllegalArgumentException {
            PresenceStatus value1;
            if(PresenceStatus.acronymMap.containsKey(acronym.toLowerCase()))
                value1 = PresenceStatus.acronymMap.get(acronym.toLowerCase());
            else {
                try {
                    value1 = PresenceStatus.valueOf(acronym.toUpperCase());
                } catch(IllegalArgumentException e) {
                    throw new IllegalArgumentException(acronym + " not understood, possible options: "
                            + StringUtils.implode(", ", PresenceStatus.acronymMap.keySet().toArray(new String[0])));
                }
            }

            return value1;
        }

    }

}
