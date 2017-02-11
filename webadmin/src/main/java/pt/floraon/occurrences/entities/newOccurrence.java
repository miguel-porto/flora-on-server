package pt.floraon.occurrences.entities;

import org.apache.commons.csv.CSVRecord;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.fieldmappers.FieldParser;

import java.util.Map;

/**
 * Created by miguel on 08-02-2017.
 */
public class newOccurrence {
    private NewSpeciesList speciesList;
    private newOBSERVED_IN observedIn;

    public NewSpeciesList getSpeciesList() {
        return speciesList;
    }

    public void setSpeciesList(NewSpeciesList speciesList) {
        this.speciesList = speciesList;
    }

    public newOBSERVED_IN getObservedIn() {
        return observedIn;
    }

    public void setObservedIn(newOBSERVED_IN observedIn) {
        this.observedIn = observedIn;
    }

    public static newOccurrence fromCSVline(CSVRecord record, Map<String, FieldParser> fieldMappers) throws FloraOnException {
        newOccurrence no = new newOccurrence();
        no.setObservedIn(new newOBSERVED_IN());
        no.setSpeciesList(new NewSpeciesList());
        Map<String, String> map = record.toMap();

        for(Map.Entry<String, String> e : map.entrySet()) {
            fieldMappers.get(e.getKey()).parseValue(e.getValue(), e.getKey(), no);
        }
        return no;
    }
}
