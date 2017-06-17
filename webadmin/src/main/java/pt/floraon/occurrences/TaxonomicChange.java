package pt.floraon.occurrences;

import pt.floraon.driver.utils.StringUtils;

/**
 * Created by miguel on 03-06-2017.
 */
public class TaxonomicChange {
    private String targetTaxEntId;
    private String[] uuids;
    private String userId;

    public TaxonomicChange(String targetTaxEntId, String uuids, String userId) {
        this.targetTaxEntId = targetTaxEntId;
        this.userId = userId;
        this.uuids = uuids.split(",");
    }

    public String getTargetTaxEntId() {
        return targetTaxEntId;
    }

    public String[] getUuids() {
        return uuids;
    }

    public String getUserId() {
        return userId;
    }
}
