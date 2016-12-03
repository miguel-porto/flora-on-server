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

    public enum DeclineDistribution implements LabelledEnum {
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

        DeclineDistribution(String desc) {
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

    public enum DeclinePopulation implements LabelledEnum {
        NO_INFORMATION("No information")
        , STABLE("Stable")
        , DECREASE_REVERSIBLE("Dec rever")
        , DECREASE_IRREVERSIBLE("Dec irrev")
        , POSSIBLE_DECREASE_FUTURE("Poss dec")
        , DECREASE_PAST_FUTURE("Dec Past fut");

        private String label;

        DeclinePopulation(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum SeverelyFragmented implements LabelledEnum {
        NO_INFORMATION("No information")
        , SEVERELY_FRAGMENTED("Severely fragmented")
        , NOT_SEVERELY_FRAGMENTED("Not severely fragmented");

        private String label;

        SeverelyFragmented(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum YesNoNA implements LabelledEnum {
        NO_DATA("No data")
        , NO("No")
        , YES("Yes");

        private String label;

        YesNoNA(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum GenerationLength implements LabelledEnum {
        NO_DATA("No data")
        , ONE_YEAR("1 year")
        , GT_ONE_YEAR("> 1 years");

        private String label;

        GenerationLength(String desc) {
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

    public enum RedListCategories implements LabelledEnum {
        EX("Extinct")
        , EW("Extinct in the Wild")
        , RE("Regionally Extinct")
        , CR("Critically Endangered")
        , EN("Endangered")
        , VU("Vulnerable")
        , NT("Near Threatened")
        , LC("Least Concern")
        , DD("Data Deficient")
        , NA("Not Applicable")
        , NE("Not Evaluated");

        private String label;

        RedListCategories(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AssessmentStatus implements LabelledEnum {
        NOT_EVALUATED("Not assessed yet")
        , READY("Ready to be assessed")
        , PRELIMINARY("Preliminary assessment")
        , REVISED_WORKING("Revised, working")
        , REVISED_PUBLISHING("Revised, ready to publish")
        , APPROVED("Approved")
        , PUBLISHED("Published");

        private String label;

        AssessmentStatus(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }
}
