package pt.floraon.redlistdata.entities;

import org.apache.http.annotation.Obsolete;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.Comparator;
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

    public static class RevisionWithTaxEntComparator implements Comparator<RevisionWithTaxEnt> {
        public int compare(RevisionWithTaxEnt o1, RevisionWithTaxEnt o2) {
            int c = o1.getDateTimeSaved().compareTo(o2.getDateTimeSaved());
            if(c == 0)
                c = o1.getTaxEnt().getName().compareTo(o2.getTaxEnt().getName());
            return c == 0 ? o1.getUser().compareTo(o2.getUser()) : c;
        }
    }

}
