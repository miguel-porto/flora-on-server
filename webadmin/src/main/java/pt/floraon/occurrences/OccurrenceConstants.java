package pt.floraon.occurrences;

import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.RedListEnums;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enums and other constants used for occurrences.
 * Created by miguel on 05-02-2017.
 */
public final class OccurrenceConstants {
    static public Map<String, OccurrenceFlavour> occurrenceManagerFlavours = new LinkedHashMap<>();
    static {
        occurrenceManagerFlavours.put("simple", new OccurrenceFlavour() {
            @Override
            public String[] getFields() {
                return new String[]{
                        "taxa", "confidence", "coordinates", "precision", "comment", "privateNote", "date", "phenoState", "observers"
                };
            }

            @Override
            public boolean showInOccurrenceView() {
                return true;
            }

            @Override
            public boolean showInInventoryView() {
                return false;
            }

            @Override
            public String getName() {
                return "Simples";
            }
        });

        occurrenceManagerFlavours.put("redlist", new OccurrenceFlavour() {
            @Override
            public String[] getFields() {
                return new String[] {
                    "date", "observers", "coordinates", "locality", "precision", "gpsCode", "taxa", "presenceStatus"
                    , "confidence", "phenoState", "abundance", "typeOfEstimate", "hasPhoto", "hasSpecimen", "specificThreats"
                    , "comment", "privateNote"};
            }

            @Override
            public boolean showInOccurrenceView() {
                return true;
            }

            @Override
            public boolean showInInventoryView() {
                return true;
            }

            @Override
            public String getName() {
                return "Red List";
            }
        });

        occurrenceManagerFlavours.put("herbarium", new OccurrenceFlavour() {
            @Override
            public String[] getFields() {
                return new String[] {
                    "accession", "taxa", "presenceStatus", "coordinates", "precision", "verbLocality", "date"
                        , "collectors", "labelData", "privateNote"};
            }

            @Override
            public boolean showInOccurrenceView() {
                return true;
            }

            @Override
            public boolean showInInventoryView() {
                return false;
            }

            @Override
            public String getName() {
                return "Herbário";
            }
        });

        occurrenceManagerFlavours.put("management", new OccurrenceFlavour() {
            @Override
            public String[] getFields() {
                return new String[] {
                        "gpsCode_accession", "coordinates", "precision", "taxa", "confidence", "date", "locality_verbLocality"
                        , "presenceStatus", "observers_collectors", "comment_labelData", "privateNote", "abundance"
                        , "typeOfEstimate", "hasPhoto", "hasSpecimen", "specificThreats", "phenoState"};
            }

            @Override
            public boolean showInOccurrenceView() {
                return true;
            }

            @Override
            public boolean showInInventoryView() {
                return false;
            }

            @Override
            public String getName() {
                return "Gestão";
            }
        });

        occurrenceManagerFlavours.put("inventory", new OccurrenceFlavour() {
            @Override
            public String[] getFields() {
                return new String[] {
                        "taxa", "confidence", "phenoState", "abundance", "coverIndex", "comment", "privateNote"};
            }

            @Override
            public boolean showInOccurrenceView() {
                return false;
            }

            @Override
            public boolean showInInventoryView() {
                return true;
            }

            @Override
            public String getName() {
                return "Inventário";
            }
        });

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
