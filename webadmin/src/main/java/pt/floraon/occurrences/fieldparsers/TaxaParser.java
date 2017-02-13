package pt.floraon.occurrences.fieldparsers;

import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;

import java.util.Date;
import java.util.List;

/**
 * Created by miguel on 08-02-2017.
 */
public class TaxaParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("")) return;

        String[] spl = inputValue.split(",");
        String tmp;
//        List<String> taxaNames = new ArrayList<>();
        List<newOBSERVED_IN> obs = occurrence.getObservedIn();
        newOBSERVED_IN n;

        for(String taxon : spl) {
            tmp = taxon.trim();
            n = new newOBSERVED_IN();
            n.setVerbTaxon(tmp);
            n.setDateInserted(new Date());
            obs.add(n);
        }
    }
}
