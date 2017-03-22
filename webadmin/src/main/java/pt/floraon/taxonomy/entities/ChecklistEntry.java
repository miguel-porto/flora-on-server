package pt.floraon.taxonomy.entities;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.ArrayUtils;
import pt.floraon.driver.Constants;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.driver.results.TaxEntAndNativeStatusResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO checklist!!
 * Created by miguel on 10-03-2017.
 */
public class ChecklistEntry extends TaxEntAndNativeStatusResult {
    static private List<Constants.TaxonRanks> checklistRanks = new ArrayList<>();

    private List<TaxEnt> higherTaxonomy;


    @Override
    public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
        TaxonomicPath taxonomicPath;
        taxonomicPath = new TaxonomicPath(this.higherTaxonomy);

        if (this.taxent == null) {
            rec.print("");
            return;
        }
//        rec.print(taxonomicPath.toString());
        TaxEnt tmp;
        for(Constants.TaxonRanks cf : Constants.CHECKLISTFIELDS) {
            if((tmp = taxonomicPath.getTaxonOfRank(cf)) == null)
                rec.print("");
            else
                rec.print(tmp.getFullName(false));
        }
        super.toCSVLine(rec, obj);
    }

    @Override
    public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException {
//        rec.print("higherTaxonomy");
        for(Constants.TaxonRanks cf : Constants.CHECKLISTFIELDS)
            rec.print(cf.getName().toLowerCase());
        super.getCSVHeader(rec, obj);
    }
}
