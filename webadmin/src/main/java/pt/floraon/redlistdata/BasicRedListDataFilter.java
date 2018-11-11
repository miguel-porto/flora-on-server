package pt.floraon.redlistdata;

import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Implements a simple filter to filter out {@link RedListDataEntity}s.
 */
public class BasicRedListDataFilter implements RedListDataFilter {
    private Set<String> filterTags;

    public BasicRedListDataFilter(Set<String> filterTags) {
        this.filterTags = filterTags;
    }

    @Override
    public boolean enter(RedListDataEntity rlde) {
        return(filterTags != null && Collections.disjoint(filterTags, Arrays.asList(rlde.getTags())));
    }
}
