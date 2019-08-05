package pt.floraon.taxonomy.entities;

/**
 * A POJO to use with QueryDriver.5: a query that returns the nearest accepted TaxEnt given any TaxEnt.
 */
public class TaxEntMatch {
    private String taxEntId;
    private TaxEnt matchedTaxEnt;

    public String getTaxEntId() {
        return taxEntId;
    }

    public void setTaxEntId(String taxEntId) {
        this.taxEntId = taxEntId;
    }

    public TaxEnt getMatchedTaxEnt() {
        return matchedTaxEnt;
    }

    public void setMatchedTaxEnt(TaxEnt matchedTaxEnt) {
        this.matchedTaxEnt = matchedTaxEnt;
    }
}
