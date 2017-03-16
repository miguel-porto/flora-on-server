package pt.floraon.redlistdata;

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
        NO_INFORMATION("PopulationSizeReduction.1", false)
        , NO_REDUCTION("PopulationSizeReduction.2", false)
        , DECREASE_REVERSIBLE("PopulationSizeReduction.3", true)
        , DECREASE_IRREVERSIBLE("PopulationSizeReduction.4", true)
        , POSSIBLE_DECREASE_FUTURE("PopulationSizeReduction.5", true)
        , DECREASE_PAST_FUTURE("PopulationSizeReduction.6", true);

        private String label;
        private boolean trigger;

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

    public enum DeclineDistribution implements TriggerEnum {
        NO_INFORMATION("DeclineDistribution.1", false)
        , NO_DECLINE("DeclineDistribution.2", false)
        , DECLINE_EOO("DeclineDistribution.3", true)
        , DECLINE_AOO("DeclineDistribution.4", true)
        , DECLINE_EOO_AOO("DeclineDistribution.5", true)
        , REDUCTION_EOO_AOO("DeclineDistribution.6", true);

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
        NO_DATA("Not estimated", false)
        , EXACT_COUNT("Exact count", true)
        , APPROXIMATE_COUNT("Approximate count", true)
        , ROUGH_ESTIMATE("Rough estimate", true);

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
    }

    public enum NrMatureIndividuals implements LabelledEnum {
        NO_DATA("NrMatureIndividuals.1")
        , GT_15000("NrMatureIndividuals.1b")
        , GT_10000("NrMatureIndividuals.2")
        , BET_2500_10000("NrMatureIndividuals.3")
        , BET_1000_2500("NrMatureIndividuals.4")
        , BET_250_1000("NrMatureIndividuals.5")
        , BET_50_250("NrMatureIndividuals.6")
        , LT_50("NrMatureIndividuals.7")
        , EXACT_NUMBER("NrMatureIndividuals.8");

        private String label;

        NrMatureIndividuals(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }

    }

    public enum DeclinePopulation implements TriggerEnum {
        NO_INFORMATION("DeclinePopulation.1", false)
        , STABLE("DeclinePopulation.2", false)
        , INCREASING("DeclinePopulation.3", false)
        , NON_CONTINUED_DECLINE("DeclinePopulation.4", false)
        , CONTINUED_DECLINE("DeclinePopulation.5", true);

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
        NO_DATA("PercentMatureOneSubpop.1")
        , BT_90_100("PercentMatureOneSubpop.2")
        , BT_95_100("PercentMatureOneSubpop.3")
        , LT_1000("PercentMatureOneSubpop.4");

        private String label;

        PercentMatureOneSubpop(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum HabitatTypes implements LabelledEnum {
        HAB_A("Habitat A")
        ,HAB_B("Habitat B")
        ,HAB_C("Habitat C");

        private String label;

        HabitatTypes(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DeclineHabitatQuality implements TriggerEnum {
        NO_INFORMATION("DeclineHabitatQuality.1", false)
        , STABLE("DeclineHabitatQuality.2", false)
        , INCREASING("DeclineHabitatQuality.3", false)
        , NON_CONTINUED_DECLINE("DeclineHabitatQuality.4", false)
        , CONTINUED_DECLINE("DeclineHabitatQuality.5", true);

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
        , NON_CONTINUED_DECLINE("DeclineNrLocations.4", false)
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

    public enum ThreatCategories implements LabelledEnum {
        HUMAN_CONSTRUCTION("ThreatCategories.1")
        , VEGETATION_MANAGEMENT("ThreatCategories.2")
        , RESOURCES("ThreatCategories.3")
        , OTHER_HUMAN("ThreatCategories.4")
        , NATURAL_DYNAMICS("ThreatCategories.5")
        , STOCHASTIC("ThreatCategories.6")
        , UNKNOWN("ThreatCategories.7")
        , OTHER("ThreatCategories.8");

        private String label;

        ThreatCategories(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return this.label;
        }
    }

    public enum Threats implements LabelledEnumWithDescription {
        CURBI("Threats.1", "Threats.1.desc", ThreatCategories.HUMAN_CONSTRUCTION)
        , CTRAN("Threats.2", "Threats.2.desc", ThreatCategories.HUMAN_CONSTRUCTION)
        , CCOST("Threats.3", "Threats.3.desc", ThreatCategories.HUMAN_CONSTRUCTION)
        , CBARR("Threats.4", "Threats.4.desc", ThreatCategories.HUMAN_CONSTRUCTION)
        , CEOLS("Threats.5", "Threats.5.desc", ThreatCategories.HUMAN_CONSTRUCTION)
        , CLINE("Threats.6", "Threats.6.desc", ThreatCategories.HUMAN_CONSTRUCTION)
        , COUTR("Threats.7", "Threats.7.desc", ThreatCategories.HUMAN_CONSTRUCTION)
        , AAGRO("Threats.8", "Threats.8.desc", ThreatCategories.VEGETATION_MANAGEMENT)
        , AFLOR("Threats.9", "Threats.9.desc", ThreatCategories.VEGETATION_MANAGEMENT)
        , APECU("Threats.10", "Threats.10.desc", ThreatCategories.VEGETATION_MANAGEMENT)
        , ACULT("Threats.11", "Threats.11.desc", ThreatCategories.VEGETATION_MANAGEMENT)
        , ADESF("Threats.12", "Threats.12.desc", ThreatCategories.VEGETATION_MANAGEMENT)
        , EXGEO("Threats.13", "Threats.13.desc", ThreatCategories.RESOURCES)
        , EXHID("Threats.14", "Threats.14.desc", ThreatCategories.RESOURCES)
        , EXSAL("Threats.15", "Threats.15.desc", ThreatCategories.RESOURCES)
        , EXAQU("Threats.16", "Threats.16.desc", ThreatCategories.RESOURCES)
        , EXREC("Threats.17", "Threats.17.desc", ThreatCategories.RESOURCES)
        , HLAZE("Threats.18", "Threats.18.desc", ThreatCategories.OTHER_HUMAN)
        , HPOLU("Threats.19", "Threats.19.desc", ThreatCategories.OTHER_HUMAN)
        , HRESI("Threats.20", "Threats.20.desc", ThreatCategories.OTHER_HUMAN)
        , HLITO("Threats.21", "Threats.21.desc", ThreatCategories.OTHER_HUMAN)
        , HFOGO("Threats.22", "Threats.22.desc", ThreatCategories.OTHER_HUMAN)
        , HDREN("Threats.23", "Threats.23.desc", ThreatCategories.OTHER_HUMAN)
        , BNATU("Threats.24", "Threats.24.desc", ThreatCategories.NATURAL_DYNAMICS)
        , BEROS("Threats.25", "Threats.25.desc", ThreatCategories.NATURAL_DYNAMICS)
        , BNEVE("Threats.26", "Threats.26.desc", ThreatCategories.NATURAL_DYNAMICS)
        , BHIDR("Threats.27", "Threats.27.desc", ThreatCategories.NATURAL_DYNAMICS)
        , BEXOT("Threats.28", "Threats.28.desc", ThreatCategories.NATURAL_DYNAMICS)
        , BHERB("Threats.29", "Threats.29.desc", ThreatCategories.NATURAL_DYNAMICS)
        , BREPR("Threats.30", "Threats.30.desc", ThreatCategories.NATURAL_DYNAMICS)
        , BDOEN("Threats.31", "Threats.31.desc", ThreatCategories.NATURAL_DYNAMICS)
        , BPOLG("Threats.32", "Threats.32.desc", ThreatCategories.NATURAL_DYNAMICS)
        , STOCA("Threats.33", "Threats.33.desc", ThreatCategories.STOCHASTIC)
        , UNKN("Threats.34", "Threats.34.desc", ThreatCategories.UNKNOWN)
        , INEXT("Threats.35", "Threats.35.desc", ThreatCategories.UNKNOWN)
        , OTHER("Threats.36", "Threats.36.desc", ThreatCategories.OTHER);

        private String label, description;
        private ThreatCategories category;

        Threats(String label, String description, ThreatCategories category) {
            this.label = label;
            this.description = description;
            this.category = category;
        }

        public ThreatCategories getCategory() {
            return this.category;
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

    public enum RedListCategories implements TriggerEnum {
        EX("Extinct", false, false, "EX")
        , EW("Extinct in the Wild", false, false, "EW")
        , RE("Regionally Extinct", false, false, "RE")
        , CR("Critically Endangered", true, false, "CR")
        , EN("Endangered", false, false, "EN")
        , VU("Vulnerable", false, false, "VU")
        , NT("Near Threatened", false, false, "NT")
        , LC("Least Concern", false, false, "LC")
        , DD("Data Deficient", false, false, "DD")
        , NA("Not Applicable", false, false, "NA")
        , NE("Not Evaluated", false, false, "NE")
        , CR_UP("Critically Endangered", true, true, "CRº")
        , EN_UP("Endangered", false, true, "ENº")
        , VU_UP("Vulnerable", false, true, "VUº")
        , NT_UP("Near Threatened", false, true, "NTº")
        , LC_DOWN("Least Concern", false, true, "LCº")
        , NT_DOWN("Near Threatened", false, true, "NTº")
        , VU_DOWN("Vulnerable", false, true, "VUº")
        , EN_DOWN("Endangered", false, true, "ENº");

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
         * Gets the effective category, after correction, without the º, i.e. CR_UP becomes CR, etc.
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
        , PE("Possibly extinct")
        , PEW("Possibly extinct in the wild")
        , PRE("Possibly regionally extinct")
        , PREW("Possibly regionally extinct in the wild");

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
        , READY("TextStatus.3");
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
        , PRELIMINARY("AssessmentStatus.2");

        private String label;

        AssessmentStatus(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }

        public boolean isAssessed() {
            return this == PRELIMINARY;
        }
    }

    public enum ReviewStatus implements LabelledEnum {
        NOT_REVISED("ReviewStatus.1")
        , REVISED_WORKING("ReviewStatus.2")
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
            return this == PUBLISHED;
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
