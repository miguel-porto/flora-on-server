package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.Constants;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;

import java.util.Date;
import java.util.List;

/**
 * Created by miguel on 08-02-2017.
 */
public class TaxaParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        if(inputValue == null) return;
        Inventory occurrence = (Inventory) bean;

        String[] spl = inputValue.split("\\+");
        if(spl.length == 1) spl = inputValue.split(",");

        String tmp;
        List<newOBSERVED_IN> obs = occurrence.getUnmatchedOccurrences();
        newOBSERVED_IN n;

        for(String taxon : spl) {
            tmp = taxon.trim();
            n = new newOBSERVED_IN(true);
            if(tmp.contains("?")) n.setConfidence(OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL);
            if(tmp.contains("#")) n.setPhenoState(Constants.PhenologicalStates.FLOWER);
            tmp = tmp.replace("?", "");
            tmp = tmp.replace("#", "");
            tmp = tmp.replace("+", "");
            n.setVerbTaxon(tmp);
            n.setDateInserted(new Date());
            obs.add(n);
        }
    }
}
