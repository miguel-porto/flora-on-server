package pt.floraon.redlistdata;

import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Implements a simple filter to filter out {@link RedListDataEntity}s.
 */
public final class RedListDataFilterFactory {

    /**
     * Filters out all taxa whose assessment is not published
     * @return
     */
    public static RedListDataFilter onlyPublished() {
        return new RedListDataFilter() {
            @Override
            public boolean enter(RedListDataEntity rlde) {
                return rlde.getAssessment().getPublicationStatus().isPublished();
            }
        };
    }

    /**
     * Filters out all taxa that were not assessed.
     * @return
     */
    public static RedListDataFilter onlyAssessed() {
        return new RedListDataFilter() {
            @Override
            public boolean enter(RedListDataEntity rlde) {
                return rlde.getAssessment().getAssessmentStatus().isAssessed();
            }
        };
    }

    /**
     * Filters in taxa that have one of the tags.
     * @param filterTags
     * @return
     */
    public static RedListDataFilter filterByTags(final Set<String> filterTags) {
        return new RedListDataFilter() {
            @Override
            public boolean enter(RedListDataEntity rlde) {
                return(filterTags == null || !Collections.disjoint(filterTags, Arrays.asList(rlde.getTags())));
            }
        };
    }
}
