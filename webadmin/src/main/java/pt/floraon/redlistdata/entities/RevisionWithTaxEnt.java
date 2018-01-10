package pt.floraon.redlistdata.entities;

import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.Objects;

public class RevisionWithTaxEnt extends Revision {
    private TaxEnt taxEnt;

    public RevisionWithTaxEnt(Revision revision) {
        this.dateSaved = revision.dateSaved;
        this.user = revision.user;
    }

    public TaxEnt getTaxEnt() {
        return taxEnt;
    }

    public void setTaxEnt(TaxEnt taxEnt) {
        this.taxEnt = taxEnt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RevisionWithTaxEnt that = (RevisionWithTaxEnt) o;
        return Objects.equals(taxEnt, that.taxEnt);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), taxEnt);
    }
}
