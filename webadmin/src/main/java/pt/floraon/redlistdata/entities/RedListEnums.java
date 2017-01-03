package pt.floraon.redlistdata.entities;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by miguel on 16-11-2016.
 */
public class RedListEnums {

    public interface LabelledEnum {
        String getLabel();
    }

    public interface TriggerEnum extends LabelledEnum {
        boolean isTrigger();
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

    public enum PopulationSizeReduction implements LabelledEnum {
        NO_INFORMATION("No information")
        , STABLE("Stable")
        , POSSIBLE_INCREASE("Possible increase")
        , DECREASE_REVERSIBLE("Population reduction observed, estimated, inferred, or suspected in the past where the " +
                "causes of the reduction are clearly reversible AND understood AND have ceased")
        , DECREASE_IRREVERSIBLE("Population reduction observed, estimated, inferred, or suspected in the past where the " +
                "causes of reduction may not have ceased OR may not be understood OR may not be reversible")
        , POSSIBLE_DECREASE_FUTURE("Population reduction projected, inferred or suspected to be met in the future " +
                "(up to a maximum of 100 years)")
        , DECREASE_PAST_FUTURE("An observed, estimated, inferred, projected or suspected population reduction where the " +
                "time period must include both the past and the future (up to a max of 100 years in future), and where " +
                "the causes of reduction may not have ceased OR may not be understood OR may not be reversible");

        private String label;

        PopulationSizeReduction(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DeclineDistribution implements TriggerEnum {
        NO_INFORMATION("No information", false)
        , NO_DECLINE("No continued decline", false)
        , DECLINE_EOO("Continued decline in EOO", true)
        , DECLINE_AOO("Continued decline in AOO", true)
        , DECLINE_EOO_AOO("Continued decline in EOO and AOO", true);

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
        NO_INFORMATION("No information", false)
        , STABLE("Stable", false)
        , INCREASING("Increasing", false)
        , CONTINUED_DECLINE("Continued decline", true);

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
        NO_INFORMATION("No information", false)
        , STABLE("Stable", false)
        , INCREASING("Increasing", false)
        , CONTINUED_DECLINE("Continued decline", true);

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

    public enum Uses implements LabelledEnum {
        USE_A("Medicinal")
        , USE_B("Edible")
        , USE_C("Ornamental");

        private String label;

        Uses(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
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

    public enum ProposedConservationActions implements LabelledEnum {
        NOT_REQUIRED("Additional studies or measures are not required")
        , POPULATION_STUDIES("Requires studies about population dynamics or ecological requirements")
        , THREAT_STUDIES("Threats should be studied")
        , LEGISLATION("Requires conservation legislation")
        , TAXONOMIC_STUDIES("Requires taxonomic studies");

        private String label;

        ProposedConservationActions(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
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
        NO_TEXT("No texts")
        , IN_PROGRESS("In progress")
        , READY("Ready to be assessed");
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
        NOT_EVALUATED("Not assessed yet")
        , PRELIMINARY("Preliminary assessment");

        private String label;

        AssessmentStatus(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }

    }

    public enum ReviewStatus implements LabelledEnum {
        NOT_REVISED("Not revised")
        , REVISED_WORKING("Revised, improvements needed")
        , REVISED_PUBLISHING("Revised, ready to publish");

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
        NOT_PUBLISHED("Not published")
        , APPROVED("Approved, ready to publish")
        , SELECTED_DISCUSSION("Selected to discussion")
        , PUBLISHED("Published");

        private String label;

        PublicationStatus(String desc) {
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


}
