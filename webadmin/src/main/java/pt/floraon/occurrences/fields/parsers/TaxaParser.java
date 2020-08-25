package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.Constants;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 08-02-2017.
 */
public class TaxaParser implements FieldParser {
    private static final Pattern coverValue = Pattern.compile("(?<cover>(?<![0-9])[0-9]{1,2}([.][0-9]+)?)$");

    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        Matcher mat;
        if(inputValue == null) return;
        Inventory occurrence = (Inventory) bean;

        String[] spl = inputValue.split("\\+");
//        if(spl.length == 1) spl = inputValue.split(",");

        String tmp;
        List<OBSERVED_IN> obs = occurrence.getUnmatchedOccurrences();
        OBSERVED_IN n;

        for(String taxon : spl) {
            tmp = taxon.trim();
            n = new OBSERVED_IN(true);
            if(tmp.contains("?")) n.setConfidence(OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL);
            if(tmp.contains("#")) n.setPhenoState(Constants.PhenologicalStates.FLOWER);
            if((mat = coverValue.matcher(tmp)).find()) {
                Float v = null;
                try {
                    v = Float.parseFloat(mat.group("cover"));
                } catch (NumberFormatException e) {
                }
                n.setCover(v);
                tmp = mat.replaceAll("");
            }

            tmp = tmp.replace("?", "");
            tmp = tmp.replace("#", "");
            tmp = tmp.replace("+", "");
            n.setVerbTaxon(tmp);
            n.setDateInserted(new Date());
            obs.add(n);
        }
    }
}
