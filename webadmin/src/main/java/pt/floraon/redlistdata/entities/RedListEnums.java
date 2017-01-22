package pt.floraon.redlistdata.entities;

import java.util.*;

/**
 * Created by miguel on 16-11-2016.
 */
public class RedListEnums {
    private static ResourceBundle myBundle = ResourceBundle.getBundle("pt.floraon.redlistdata.fieldValues", Locale.forLanguageTag("pt"));

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

    public enum TypeOfPopulationEstimate implements LabelledEnum {
        NO_DATA("Not estimated")
        , EXACT_COUNT("Exact count")
        , APPROXIMATE_COUNT("Approximate count")
        , ROUGH_ESTIMATE("Rough estimate");

        private String label;

        TypeOfPopulationEstimate(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum NrMatureIndividuals implements LabelledEnum {
        NO_DATA("No data")
        , GT_10000("> 10000")
        , BET_2500_10000("2500 - 10000")
        , BET_250_2500("250 - 2500")
        , LT_250("< 250")
        , EXACT_NUMBER("Exact number");

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
        NO_DATA("No data")
        , LT_80("< 80%")
        , BT_80_90("80% - 90%")
        , BT_90_95("90% - 95%")
        , BT_95_100("95 - 100%")
        , LT_1000("100%");

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
        NO_INFORMATION("No information", false)
        , STABLE("Stable", false)
        , INCREASING("Increasing", false)
        , CONTINUED_DECLINE("Continued decline", true);

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


}
