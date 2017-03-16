package pt.floraon.taxonomy.entities;

import org.apache.commons.csv.CSVPrinter;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.driver.results.TaxEntAndNativeStatusResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * TODO checklist!!
 * Created by miguel on 10-03-2017.
 */
public class ChecklistEntry extends TaxEntAndNativeStatusResult {
    List<TaxEnt> higherTaxonomy;

    public List<TaxEnt> getHigherTaxonomy() {
        return higherTaxonomy;
    }

    @Override
    public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
        if (this.taxent == null) {
            rec.print("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (TaxEnt te : this.higherTaxonomy) {
            sb.append(te.getFullName()).append(" : ");
        }
        rec.print(sb.toString());
        super.toCSVLine(rec, obj);
    }

    @Override
    public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException {
        rec.print("higherTaxonomy");
        super.getCSVHeader(rec, obj);
    }
}
