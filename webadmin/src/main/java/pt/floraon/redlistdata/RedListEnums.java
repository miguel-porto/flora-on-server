package pt.floraon.redlistdata;

import pt.floraon.driver.utils.StringUtils;

import java.util.*;

/**
 * Created by miguel on 16-11-2016.
 */
public class RedListEnums {
//    private static ResourceBundle myBundle = ResourceBundle.getBundle("pt.floraon.redlistdata.fieldValues", Locale.forLanguageTag("pt"));

    public interface LabelledEnum {
        String getLabel();
    }

    public interface TriggerEnum extends LabelledEnum {
        boolean isTrigger();
    }

    public interface LabelledEnumWithDescription extends LabelledEnum {
        String getDescription();
    }

    public static <E extends Enum<E>> String[] getEnumValuesAsString(Class<E> clazz) {
        List<String> out = new ArrayList<String>();

        for(E v : EnumSet.allOf(clazz)) {
            out.add(v.toString());
        }
        return out.toArray(new String[out.size()]);
    }

    public static <E extends Enum<E> & LabelledEnum> String[] getEnumLabelsAsString(Class<E> clazz) {
        List<String> out = new ArrayList<String>();

        for(E v : EnumSet.allOf(clazz)) {
            out.add(v.getLabel());
        }
        return out.toArray(new String[out.size()]);
    }

    public enum PopulationSizeReduction implements TriggerEnum {
        NO_INFORMATION(FieldValues.getString("PopulationSizeReduction.1"), false)
        , NO_REDUCTION(FieldValues.getString("PopulationSizeReduction.2"), false)
        , DECREASE_REVERSIBLE(FieldValues.getString("PopulationSizeReduction.3"), true)
        , DECREASE_IRREVERSIBLE(FieldValues.getString("PopulationSizeReduction.4"), true)
        , POSSIBLE_DECREASE_FUTURE(FieldValues.getString("PopulationSizeReduction.5"), true)
        , DECREASE_PAST_FUTURE(FieldValues.getString("PopulationSizeReduction.6"), true);

        private final String label;
        private final boolean trigger;

        PopulationSizeReduction(String desc, boolean trigger) {
            this.label = desc;
            this.trigger = trigger;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return this.trigger;
        }

    }

    public enum DeclineQualifier implements LabelledEnum {
        NO_INFORMATION("No information"),
        OBSERVED("Observed"),
        ESTIMATED("Estimated"),
        PROJECTED("Projected"),
        INFERRED("Inferred"),
        SUSPECTED("Suspected");
        private final String label;

        DeclineQualifier(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return this.label;
        }
    }

    public enum DeclineDistribution implements TriggerEnum {
        NO_INFORMATION(FieldValues.getString("DeclineDistribution.1"), false)
        , NO_DECLINE(FieldValues.getString("DeclineDistribution.2"), false)
        , DECLINE_EOO(FieldValues.getString("DeclineDistribution.3"), true)
        , DECLINE_AOO(FieldValues.getString("DeclineDistribution.4"), true)
        , DECLINE_EOO_AOO(FieldValues.getString("DeclineDistribution.5"), true)
        , REDUCTION_EOO_AOO(FieldValues.getString("DeclineDistribution.6"), true);

        private String label;
        private boolean trigger;

        DeclineDistribution(String desc, boolean trigger) {
            this.label = desc;
            this.trigger = trigger;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return this.trigger;
        }
    }

    public enum ExtremeFluctuations implements LabelledEnum {
        NO_INFORMATION("No information")
        , NOT_APPLICABLE("Not applicable")
        , EOO("Yes, in EOO")
        , AOO("Yes, in AOO")
        , EOO_AOO("Yes, in EOO and AOO");
        private String label;

        ExtremeFluctuations(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum TypeOfPopulationEstimate implements TriggerEnum {
        NO_DATA(FieldValues.getString("TypeOfEstimate.1"), false)
        , EXACT_COUNT(FieldValues.getString("TypeOfEstimate.2"), true)
        , APPROXIMATE_COUNT(FieldValues.getString("TypeOfEstimate.3"), true)
        , ROUGH_ESTIMATE(FieldValues.getString("TypeOfEstimate.4"), true)
        , NULL(FieldValues.getString("TypeOfEstimate.5"), false);

        private static Map<String, TypeOfPopulationEstimate> acronymMap = new HashMap<>();
        static {
            acronymMap.put("e", TypeOfPopulationEstimate.APPROXIMATE_COUNT);
            acronymMap.put("estimativa", TypeOfPopulationEstimate.APPROXIMATE_COUNT);
            acronymMap.put("estimate", TypeOfPopulationEstimate.APPROXIMATE_COUNT);
            acronymMap.put("c", TypeOfPopulationEstimate.EXACT_COUNT);
            acronymMap.put("contagem", TypeOfPopulationEstimate.EXACT_COUNT);
            acronymMap.put("count", TypeOfPopulationEstimate.EXACT_COUNT);
            acronymMap.put("g", TypeOfPopulationEstimate.ROUGH_ESTIMATE);
            acronymMap.put("grosseira", TypeOfPopulationEstimate.ROUGH_ESTIMATE);
            acronymMap.put("rough", TypeOfPopulationEstimate.ROUGH_ESTIMATE);
            acronymMap.put("", TypeOfPopulationEstimate.NULL);
        }

        private String label;
        private boolean trigger;

        TypeOfPopulationEstimate(String desc, boolean trigger) {
            this.label = desc;
            this.trigger = trigger;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return this.trigger;
        }

        static public TypeOfPopulationEstimate getValueFromAcronym(String acronym) throws IllegalArgumentException {
            TypeOfPopulationEstimate value1;
            if(TypeOfPopulationEstimate.acronymMap.containsKey(acronym.toLowerCase()))
                value1 = TypeOfPopulationEstimate.acronymMap.get(acronym.toLowerCase());
            else {
                try {
                    value1 = TypeOfPopulationEstimate.valueOf(acronym.toUpperCase());
                } catch(IllegalArgumentException e) {
                    throw new IllegalArgumentException(acronym + " not understood, possible options: "
                            + StringUtils.implode(", ", TypeOfPopulationEstimate.acronymMap.keySet().toArray(new String[0])));
                }
            }

            return value1;
        }
    }

    public enum NrMatureIndividuals implements LabelledEnum {
        NO_DATA(FieldValues.getString("NrMatureIndividuals.1"), null)
        , GT_15000(FieldValues.getString("NrMatureIndividuals.1b"), 7)
        , GT_10000(FieldValues.getString("NrMatureIndividuals.2"), 6)
        , BET_2500_10000(FieldValues.getString("NrMatureIndividuals.3"), 5)
        , BET_1000_2500(FieldValues.getString("NrMatureIndividuals.4"), 4)
        , BET_250_1000(FieldValues.getString("NrMatureIndividuals.5"), 3)
        , BET_50_250(FieldValues.getString("NrMatureIndividuals.6"), 2)
        , LT_50(FieldValues.getString("NrMatureIndividuals.7"), 1)
        , EXACT_NUMBER(FieldValues.getString("NrMatureIndividuals.8"), null);

        private String label;
        private Integer serial;

        NrMatureIndividuals(String desc, Integer serial) {
            this.label = desc;
            this.serial = serial;
        }

        public Boolean isLessThanOrEqual(NrMatureIndividuals cmp) {
            if(this.serial == null || cmp == null) return null;
            return this.serial <= cmp.serial;
        }

        public Integer getSerial() {return this.serial;}

        @Override
        public String getLabel() {
            return label;
        }

    }

    public enum DeclinePopulation implements TriggerEnum {
        NO_INFORMATION(FieldValues.getString("DeclinePopulation.1"), false)
        , STABLE(FieldValues.getString("DeclinePopulation.2"), false)
        , INCREASING(FieldValues.getString("DeclinePopulation.3"), false)
        , NON_CONTINUED_DECLINE(FieldValues.getString("DeclinePopulation.4"), true)
        , CONTINUED_DECLINE(FieldValues.getString("DeclinePopulation.5"), true);

        private String label;
        private boolean trigger;

        DeclinePopulation(String desc, boolean trigger) {
            this.label = desc;
            this.trigger = trigger;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return trigger;
        }
    }

    public enum SeverelyFragmented implements TriggerEnum {
        NO_INFORMATION("No information", false)
        , SEVERELY_FRAGMENTED("Severely fragmented", true)
        , NOT_SEVERELY_FRAGMENTED("Not severely fragmented", false);

        private String label;
        private boolean trigger;

        SeverelyFragmented(String desc, boolean trigger) {
            this.label = desc;
            this.trigger = trigger;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return this.trigger;
        }
    }

    public enum YesNoNA implements TriggerEnum {
        NO_DATA("No information", false)
        , NO("No", false)
        , YES("Yes", true);

        private String label;
        private boolean trigger;

        YesNoNA(String desc, boolean trigger) {
            this.label = desc;
            this.trigger = trigger;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return this.trigger;
        }
    }

    public enum NrMatureEachSubpop implements LabelledEnum {
        NO_DATA("No data")
        , LT_50("≤ 50")
        , LT_250("≤ 250")
        , LT_1000("≤ 1000")
        , LT_1500("≤ 1500")
        , GT_1500("> 1500");

        private String label;

        NrMatureEachSubpop(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum PercentMatureOneSubpop implements LabelledEnum {
        NO_DATA(FieldValues.getString("PercentMatureOneSubpop.1"))
        , BT_90_100(FieldValues.getString("PercentMatureOneSubpop.2"))
        , BT_95_100(FieldValues.getString("PercentMatureOneSubpop.3"))
        , LT_1000(FieldValues.getString("PercentMatureOneSubpop.4"));

        private String label;

        PercentMatureOneSubpop(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DeclineHabitatQuality implements TriggerEnum {
        NO_INFORMATION(FieldValues.getString("DeclineHabitatQuality.1"), false)
        , STABLE(FieldValues.getString("DeclineHabitatQuality.2"), false)
        , INCREASING(FieldValues.getString("DeclineHabitatQuality.3"), false)
        , NON_CONTINUED_DECLINE(FieldValues.getString("DeclineHabitatQuality.4"), true)
        , CONTINUED_DECLINE(FieldValues.getString("DeclineHabitatQuality.5"), true);

        private String label;
        private boolean trigger;

        DeclineHabitatQuality(String desc, boolean trigger) {
            this.label = desc;
            this.trigger = trigger;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return trigger;
        }
    }

    public enum DeclineNrLocations implements TriggerEnum {
        NO_INFORMATION("DeclineNrLocations.1", false)
        , STABLE("DeclineNrLocations.2", false)
        , INCREASING("DeclineNrLocations.3", false)
        , NON_CONTINUED_DECLINE("DeclineNrLocations.4", true)
        , CONTINUED_DECLINE("DeclineNrLocations.5", true);

        private String label;
        private boolean trigger;

        DeclineNrLocations(String desc, boolean trigger) {
            this.label = desc;
            this.trigger = trigger;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return trigger;
        }
    }

    public enum Uses implements LabelledEnumWithDescription {
        FOOD_HUMAN("Uses.1", "Uses.1.desc")
        , FOOD_ANIMAL("Uses.2", "Uses.2.desc")
        , BEE_PLANT("Uses.3", "Uses.3.desc")
        , MEDICINES("Uses.4", "Uses.4.desc")
        , POISONS("Uses.5", "Uses.5.desc")
        , FUELS("Uses.6", "Uses.6.desc")
        , ORNAMENTAL("Uses.7", "Uses.7.desc")
        , HANDICRAFTS("Uses.8", "Uses.8.desc")
        , WOOD_CORK("Uses.9", "Uses.9.desc")
        , FIBRES("Uses.10", "Uses.10.desc")
        , TANNINS("Uses.11", "Uses.11.desc")
        , GUMS_RESINS("Uses.12", "Uses.12.desc")
        , OTHER_MATERIALS("Uses.13", "Uses.13.desc")
        , NATURAL_ENGINEERING("Uses.14", "Uses.14.desc")
        , GENE_SOURCE("Uses.15", "Uses.15.desc")
        , OTHER("Uses.16", "Uses.16.desc")
        , UNKNOWN("Uses.17", "Uses.17.desc");

        private String label, description;

        Uses(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    public enum Overexploitation implements LabelledEnum {
        NO_DATA("No information")
        , OVEREXPLOITED("Potentially threatened by overexploitation")
        , EXPLOITED("Exploited but not overexploited")
        , NOT_EXPLOITED("Not exploited");

        private String label;

        Overexploitation(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /*    public enum Threats implements LabelledEnumWithDescription {
        CURBI("Threats.1", "Threats.1.desc", ThreatCategories.HUMAN_CONSTRUCTION, ThreatTypes.CONSTRUCTION)
        , CTRAN("Threats.2", "Threats.2.desc", ThreatCategories.HUMAN_CONSTRUCTION, ThreatTypes.CONSTRUCTION)
        , CCOST("Threats.3", "Threats.3.desc", ThreatCategories.HUMAN_CONSTRUCTION, ThreatTypes.OTHER)
        , CBARR("Threats.4", "Threats.4.desc", ThreatCategories.HUMAN_CONSTRUCTION, ThreatTypes.HYDRIC_RESOURCES)
        , CEOLS("Threats.5", "Threats.5.desc", ThreatCategories.HUMAN_CONSTRUCTION, ThreatTypes.CONSTRUCTION)
        , CLINE("Threats.6", "Threats.6.desc", ThreatCategories.HUMAN_CONSTRUCTION, ThreatTypes.CONSTRUCTION)
        , COUTR("Threats.7", "Threats.7.desc", ThreatCategories.HUMAN_CONSTRUCTION, ThreatTypes.CONSTRUCTION)
        , AAGRO("Threats.8", "Threats.8.desc", ThreatCategories.VEGETATION_MANAGEMENT, ThreatTypes.AGRICULTURE)
        , AFLOR("Threats.9", "Threats.9.desc", ThreatCategories.VEGETATION_MANAGEMENT, ThreatTypes.FORESTRY)
        , APECU("Threats.10", "Threats.10.desc", ThreatCategories.VEGETATION_MANAGEMENT, ThreatTypes.CATTLE)
        , ACULT("Threats.11", "Threats.11.desc", ThreatCategories.VEGETATION_MANAGEMENT, ThreatTypes.MANAGEMENT)
        , ADESF("Threats.12", "Threats.12.desc", ThreatCategories.VEGETATION_MANAGEMENT, ThreatTypes.MANAGEMENT)
        , EXGEO("Threats.13", "Threats.13.desc", ThreatCategories.RESOURCES, ThreatTypes.RESOURCES)
        , EXHID("Threats.14", "Threats.14.desc", ThreatCategories.RESOURCES, ThreatTypes.HYDRIC_RESOURCES)
        , EXSAL("Threats.15", "Threats.15.desc", ThreatCategories.RESOURCES, ThreatTypes.OTHER)
        , EXAQU("Threats.16", "Threats.16.desc", ThreatCategories.RESOURCES, ThreatTypes.OTHER)
        , EXREC("Threats.17", "Threats.17.desc", ThreatCategories.RESOURCES, ThreatTypes.OTHER)
        , HLAZE("Threats.18", "Threats.18.desc", ThreatCategories.OTHER_HUMAN, ThreatTypes.OTHER_HUMAN)
        , HPOLU("Threats.19", "Threats.19.desc", ThreatCategories.OTHER_HUMAN, ThreatTypes.RESIDUALS)
        , HRESI("Threats.20", "Threats.20.desc", ThreatCategories.OTHER_HUMAN, ThreatTypes.RESIDUALS)
        , HLITO("Threats.21", "Threats.21.desc", ThreatCategories.OTHER_HUMAN, ThreatTypes.OTHER_HUMAN)
        , HFOGO("Threats.22", "Threats.22.desc", ThreatCategories.OTHER_HUMAN, ThreatTypes.FIRE)
        , HDREN("Threats.23", "Threats.23.desc", ThreatCategories.OTHER_HUMAN, ThreatTypes.HYDRIC_RESOURCES)
        , BNATU("Threats.24", "Threats.24.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.DYNAMICS)
        , BEROS("Threats.25", "Threats.25.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.EROSION)
        , BNEVE("Threats.26", "Threats.26.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.CLIMATIC)
        , BHIDR("Threats.27", "Threats.27.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.CLIMATIC)
        , BEXOT("Threats.28", "Threats.28.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.EXOTIC_SPECIES)
        , BHERB("Threats.29", "Threats.29.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.OTHER)
        , BREPR("Threats.30", "Threats.30.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.BIOLOGICAL)
        , BDOEN("Threats.31", "Threats.31.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.BIOLOGICAL)
        , BPOLG("Threats.32", "Threats.32.desc", ThreatCategories.NATURAL_DYNAMICS, ThreatTypes.BIOLOGICAL)
        , STOCA("Threats.33", "Threats.33.desc", ThreatCategories.STOCHASTIC, ThreatTypes.STOCHASTIC_UNKNOWN)
        , UNKN("Threats.34", "Threats.34.desc", ThreatCategories.UNKNOWN, ThreatTypes.STOCHASTIC_UNKNOWN)
        , INEXT("Threats.35", "Threats.35.desc", ThreatCategories.UNKNOWN, ThreatTypes.NONSIGNIFICANT)
        , OTHER("Threats.36", "Threats.36.desc", ThreatCategories.OTHER, ThreatTypes.OTHER);

        private String label, description;
        private ThreatCategories category;
        private ThreatTypes type;

        Threats(String label, String description, ThreatCategories category, ThreatTypes type) {
            this.label = label;
            this.description = description;
            this.category = category;
            this.type = type;
        }

        public ThreatCategories getCategory() {
            return this.category;
        }

        public ThreatTypes getType() {
            return this.type;
        }

        @Override
        public String getLabel() {
            return this.label;
        }

        @Override
        public String getDescription() {
            return this.description;
        }
    }
*/
    public enum ProposedConservationActions implements LabelledEnumWithDescription {
        NO_MEASURES("ProposedConservationActions.0", "ProposedConservationActions.0.desc"),
        SITE_PROTECTION("ProposedConservationActions.1", "ProposedConservationActions.1.desc"),
        INVASIVE_CONTROL("ProposedConservationActions.2", "ProposedConservationActions.2.desc"),
        HABITAT_RESTORATION("ProposedConservationActions.3", "ProposedConservationActions.3.desc"),
        REPOPULATION("ProposedConservationActions.4", "ProposedConservationActions.4.desc"),
        REINTRODUCTION("ProposedConservationActions.5", "ProposedConservationActions.5.desc"),
        OTHER("ProposedConservationActions.6", "ProposedConservationActions.6.desc"),
        ARTIFICIAL_PROPAGATION("ProposedConservationActions.7", "ProposedConservationActions.7.desc"),
        GENOME_BANK("ProposedConservationActions.8", "ProposedConservationActions.8.desc"),
        AWARENESS("ProposedConservationActions.9", "ProposedConservationActions.9.desc"),
        NEW_LEGISLATION("ProposedConservationActions.10", "ProposedConservationActions.10.desc"),
        LEGISLATION_ENFORCEMENT("ProposedConservationActions.11", "ProposedConservationActions.11.desc"),
        INCENTIVES("ProposedConservationActions.12", "ProposedConservationActions.12.desc");

        private String label, description;

        ProposedConservationActions(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    public enum ProposedStudyMeasures implements LabelledEnumWithDescription {
        NO_STUDIES("ProposedStudyMeasures.1", "ProposedStudyMeasures.1.desc"),
        TAXONOMY("ProposedStudyMeasures.2", "ProposedStudyMeasures.2.desc"),
        POPULATION("ProposedStudyMeasures.3", "ProposedStudyMeasures.3.desc"),
        ENVIRONMENTAL("ProposedStudyMeasures.4", "ProposedStudyMeasures.4.desc"),
        THREATS("ProposedStudyMeasures.5", "ProposedStudyMeasures.5.desc"),
        CONSERVATION("ProposedStudyMeasures.6", "ProposedStudyMeasures.6.desc"),
        MONITORING("ProposedStudyMeasures.7", "ProposedStudyMeasures.7.desc"),
        OTHER("ProposedStudyMeasures.8", "ProposedStudyMeasures.8.desc");

        private String label, description;

        ProposedStudyMeasures(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    public enum HasPhoto implements LabelledEnum {
        FALSE("No"), TRUE("Yes"), THREAT("Threat"), SPECIMEN_THREAT("Spec+Threat"), NULL("");
        private static Map<String, HasPhoto> acronymMap = new HashMap<>();
        static {
            acronymMap.put("s", TRUE);
            acronymMap.put("x", TRUE);
            acronymMap.put("y", TRUE);
            acronymMap.put("1", TRUE);
            acronymMap.put("yes", TRUE);
            acronymMap.put("a", THREAT);
            acronymMap.put("t", THREAT);
            acronymMap.put("threat", THREAT);
            acronymMap.put("sa", SPECIMEN_THREAT);
            acronymMap.put("as", SPECIMEN_THREAT);
            acronymMap.put("ax", SPECIMEN_THREAT);
            acronymMap.put("xa", SPECIMEN_THREAT);
            acronymMap.put("", HasPhoto.NULL);
        }

        private String label;

        HasPhoto(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return this.label;
        }

        static public HasPhoto getValueFromAcronym(String acronym) throws IllegalArgumentException {
            HasPhoto value1;
            if(HasPhoto.acronymMap.containsKey(acronym.toLowerCase()))
                value1 = HasPhoto.acronymMap.get(acronym.toLowerCase());
            else {
                try {
                    value1 = HasPhoto.valueOf(acronym.toUpperCase());
                } catch(IllegalArgumentException e) {
                    throw new IllegalArgumentException(acronym + " not understood, possible options: "
                            + StringUtils.implode(", ", HasPhoto.acronymMap.keySet().toArray(new String[0])));
                }
            }

            return value1;
        }

    }

    public enum RedListCategories implements TriggerEnum {
        EX(FieldValues.getString("category.EX"), false, false, "EX")
        , EW(FieldValues.getString("category.EW"), false, false, "EW")
        , RE(FieldValues.getString("category.RE"), false, false, "RE")
        , CR(FieldValues.getString("category.CR"), true, false, "CR")
        , EN(FieldValues.getString("category.EN"), false, false, "EN")
        , VU(FieldValues.getString("category.VU"), false, false, "VU")
        , NT(FieldValues.getString("category.NT"), false, false, "NT")
        , LC(FieldValues.getString("category.LC"), false, false, "LC")
        , DD(FieldValues.getString("category.DD"), false, false, "DD")
        , NA(FieldValues.getString("category.NA"), false, false, "NA")
        , NE(FieldValues.getString("category.NE"), false, false, "NE")
        , NOT_ASSIGNED(FieldValues.getString("category.notassigned"), false, false, "?")
        , CR_UP(FieldValues.getString("category.CR") + "º", true, true, "CRº")
        , EN_UP(FieldValues.getString("category.EN") + "º", false, true, "ENº")
        , VU_UP(FieldValues.getString("category.VU") + "º", false, true, "VUº")
        , NT_UP(FieldValues.getString("category.NT") + "º", false, true, "NTº")
        , LC_DOWN(FieldValues.getString("category.LC") + "º", false, true, "LCº")
        , NT_DOWN(FieldValues.getString("category.NT") + "º", false, true, "NTº")
        , VU_DOWN(FieldValues.getString("category.VU") + "º", false, true, "VUº")
        , EN_DOWN(FieldValues.getString("category.EN") + "º", false, true, "ENº");

        private String label;
        private String shortTag;
        private boolean trigger;
        private boolean isUpDownListed;

        RedListCategories(String desc, boolean trigger, boolean isUpDownListed, String shortTag) {
            this.label = desc;
            this.trigger = trigger;
            this.isUpDownListed = isUpDownListed;
            this.shortTag = shortTag;
        }

        public static RedListCategories[] valuesNotUpDownListed() {
            List<RedListCategories> out = new ArrayList<>();
            for(RedListCategories c : RedListCategories.values()) {
                if(!c.isUpDownListed) out.add(c);
            }
            return out.toArray(new RedListCategories[out.size()]);
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isTrigger() {
            return trigger;
        }

        public boolean isThreatened() {
            return this.getEffectiveCategory() == CR || this.getEffectiveCategory() == EN
                     || this.getEffectiveCategory() == VU;
//|| this.getEffectiveCategory() == EW || this.getEffectiveCategory() == EX || this.getEffectiveCategory() == RE
        }

        public boolean isPossiblyExtinct() {
            RedListCategories cat = this.getEffectiveCategory();
            return cat == EW || cat == EX || cat == RE;
        }

        public RedListCategories getUplistCategory() {
            switch(this) {
                case EN: return CR_UP;
                case VU: return EN_UP;
                case NT: return VU_UP;
                case LC: return NT_UP;
                default: return this;
            }
        }

        public RedListCategories getDownlistCategory() {
            switch(this) {
                case CR: return EN_DOWN;
                case EN: return VU_DOWN;
                case VU: return NT_DOWN;
                case NT: return LC_DOWN;
                default: return this;
            }
        }

        /**
         * Gets the category before the eventual down or uplisting
         * @return
         */
        public RedListCategories getOriginalCategory() {
            switch(this) {
                case CR_UP: return EN;
                case EN_DOWN: return CR;
                case EN_UP: return VU;
                case VU_DOWN: return EN;
                case VU_UP: return NT;
                case NT_DOWN: return VU;
                case NT_UP: return LC;
                case LC_DOWN: return NT;
                default: return this;
            }
        }

        /**
         * Gets the effective category after correction, without the º, i.e. CR_UP becomes CR, etc.
         * This is the "canonical" category that should be used for filters & so on.
         * @return
         */
        public RedListCategories getEffectiveCategory() {
            switch(this) {
                case CR_UP: return CR;
                case EN_DOWN: return EN;
                case EN_UP: return EN;
                case VU_DOWN: return VU;
                case VU_UP: return VU;
                case NT_DOWN: return NT;
                case NT_UP: return NT;
                case LC_DOWN: return LC;
                default: return this;
            }
        }

        public boolean isUpDownListed() {
            return isUpDownListed;
        }

        public String getShortTag() {
            return this.shortTag;
        }
    }

    public enum CRTags implements LabelledEnum {
        NO_TAG("No tag")
        , PE(FieldValues.getString("subcategory.PE"))
        , PEW(FieldValues.getString("subcategory.PEW"))
        , PRE(FieldValues.getString("subcategory.PRE"))
        , PREW(FieldValues.getString("subcategory.PREW"));

        private String label;

        CRTags(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum TextStatus implements LabelledEnum {
        NO_TEXT("TextStatus.1")
        , IN_PROGRESS("TextStatus.2")
        , READY("TextStatus.3")
        , REVISION_READY("TextStatus.4");
        private String label;

        TextStatus(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AssessmentStatus implements LabelledEnum {
        NOT_EVALUATED("AssessmentStatus.1")
        , PRELIMINARY("AssessmentStatus.2")
        , READY_REASSESSMENT("AssessmentStatus.3");

        private String label;

        AssessmentStatus(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }

        public boolean isAssessed() {
            return this == PRELIMINARY || this == READY_REASSESSMENT;
        }
    }

    public enum ReviewStatus implements LabelledEnum {
        NOT_REVISED("ReviewStatus.1")
        , REVISED_WORKING("ReviewStatus.2")
        , REVISED_MAJOR("ReviewStatus.2a")
        , REVISED_PUBLISHING("ReviewStatus.3");

        private String label;

        ReviewStatus(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }

    }

    public enum PublicationStatus implements LabelledEnum {
        NOT_PUBLISHED("PublicationStatus.1")
        , APPROVED("PublicationStatus.2")
        , SELECTED_DISCUSSION("PublicationStatus.3")
        , PUBLISHED_DRAFT("PublicationStatus.4a")
        , PUBLISHED("PublicationStatus.4");

        private String label;

        PublicationStatus(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }

        public boolean isPublished() {
            return this == PUBLISHED || this == PUBLISHED_DRAFT;
        }

        public boolean isApproved() {
            return this == APPROVED;
        }

    }

    public enum ValidationStatus implements LabelledEnum {
        IN_ANALYSIS(FieldValues.getString("ValidationStatus.1"))
        , VALIDATED(FieldValues.getString("ValidationStatus.2"))
        , NEEDS_CORRECTIONS(FieldValues.getString("ValidationStatus.3"));

        private String label;

        ValidationStatus(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum YesNoLikelyUnlikely implements LabelledEnum {
        NOT_KNOWN("Not known")
        , YES("Yes")
        , NO("No")
        , LIKELY("Likely")
        , UNLIKELY("Unlikely");

        private String label;

        YesNoLikelyUnlikely(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum UpDownList implements LabelledEnum {
        NONE("Keep same category")
        , UPLIST("Uplist")
        , DOWNLIST("Downlist");

        private String label;

        UpDownList(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AssessmentCriteria implements LabelledEnum {
        A1a         ("A1a", "A", "1", "a", null, false)
        , A1b       ("A1b", "A", "1", "b", null, false)
        , A1c       ("A1c", "A", "1", "c", null, false)
        , A1d       ("A1d", "A", "1", "d", null, false)
        , A1e       ("A1e", "A", "1", "e", null, true)
        , A2a       ("A2a", "A", "2", "a", null, false)
        , A2b       ("A2b", "A", "2", "b", null, false)
        , A2c       ("A2c", "A", "2", "c", null, false)
        , A2d       ("A2d", "A", "2", "d", null, false)
        , A2e       ("A2e", "A", "2", "e", null, true)
        , A3b       ("A3b", "A", "3", "b", null, false)
        , A3c       ("A3c", "A", "3", "c", null, false)
        , A3d       ("A3d", "A", "3", "d", null, false)
        , A3e       ("A3e", "A", "3", "e", null, true)
        , A4a       ("A4a", "A", "4", "a", null, false)
        , A4b       ("A4b", "A", "4", "b", null, false)
        , A4c       ("A4c", "A", "4", "c", null, false)
        , A4d       ("A4d", "A", "4", "d", null, false)
        , A4e       ("A4e", "A", "4", "e", null, true)

        , B1a       ("B1a", "B", "1", "a", null, true)
        , B1bi      ("B1b(i)", "B", "1", "b", "i", false)
        , B1bii     ("B1b(ii)", "B", "1", "b", "ii", false)
        , B1biii    ("B1b(iii)", "B", "1", "b", "iii", false)
        , B1biv     ("B1b(iv)", "B", "1", "b", "iv", false)
        , B1bv      ("B1b(v)", "B", "1", "b", "v", true)
        , B1ci      ("B1c(i)", "B", "1", "c", "i", false)
        , B1cii     ("B1c(ii)", "B", "1", "c", "ii", false)
        , B1ciii    ("B1c(iii)", "B", "1", "c", "iii", false)
        , B1civ     ("B1c(iv)", "B", "1", "c", "iv", true)
        , B2a       ("B2a", "B", "2", "a", null, true)
        , B2bi      ("B2b(i)", "B", "2", "b", "i", false)
        , B2bii     ("B2b(ii)", "B", "2", "b", "ii", false)
        , B2biii    ("B2b(iii)", "B", "2", "b", "iii", false)
        , B2biv     ("B2b(iv)", "B", "2", "b", "iv", false)
        , B2bv      ("B2b(v)", "B", "2", "b", "v", true)
        , B2ci      ("B2c(i)", "B", "2", "c", "i", false)
        , B2cii     ("B2c(ii)", "B", "2", "c", "ii", false)
        , B2ciii    ("B2c(iii)", "B", "2", "c", "iii", false)
        , B2civ     ("B2c(iv)", "B", "2", "c", "iv", true)

        , C1        ("C1", "C", "1", null, null, false)
        , C2ai      ("C2a(i)", "C", "2", "a", "i", false)
        , C2aii     ("C2a(ii)", "C", "2", "a", "ii", false)
        , C2b       ("C2b", "C", "2", "b", null, false)

        , D         ("D", "D", null, null, null, false)
        , D1        ("D1", "D", "1", null, null, false)
        , D2        ("D2", "D", "2", null, null, false)

        , E         ("E", "E", null, null, null, false);

        private String label, criteria, subCriteria, subsubCriteria, subsubsubCriteria;
        private boolean isBreak;

        AssessmentCriteria(String desc, String criteria, String subCriteria, String subsubCriteria, String subsubsubCriteria, boolean isBreak) {
            this.label = desc;
            this.criteria = criteria;
            this.subCriteria = subCriteria;
            this.subsubCriteria = subsubCriteria;
            this.subsubsubCriteria = subsubsubCriteria;
            this.isBreak = isBreak;
        }

        @Override
        public String getLabel() {
            return label;
        }

        public String getCollapsedLabel() {
            return subCriteria + subsubCriteria + subsubsubCriteria;
        }

        public String getCriteria() { return criteria;}

        public String getSubCriteria() {
            return subCriteria == null ? "" : subCriteria;
        }

        public String getSubsubCriteria() {
            return subsubCriteria == null ? "" : subsubCriteria;
        }

        public String getSubsubsubCriteria() {
            return subsubsubCriteria == null ? "" : subsubsubCriteria;
        }

        public boolean isBreak() {
            return isBreak;
        }

        public static List<AssessmentCriteria> getSubCriteriaOf(String criteria) {
            List<AssessmentCriteria> out = new ArrayList<>();
            for(AssessmentCriteria ac : AssessmentCriteria.values()) {
                if(ac.getCriteria().equals(criteria)) out.add(ac);
            }
            return out;
        }

        public static List<String> getCriteriaLetters() {
            return Arrays.asList("A", "B", "C", "D", "E");
        }
    }
}
