package pt.floraon.redlistdata;

import pt.floraon.redlistdata.entities.RedListDataEntity;

/**
 * A filter to filter {@link RedListDataEntity}.
 */
public interface RedListDataFilter {
    boolean enter(RedListDataEntity rlde);
}
