package pt.floraon.occurrences.arangodb;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.google.gson.Gson;
import pt.floraon.driver.*;
import pt.floraon.occurrences.StatisticPerTaxon;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by miguel on 08-07-2017.
 */
public class OccurrenceReportArangoDriver extends GOccurrenceReportDriver implements IOccurrenceReportDriver {
    public enum TaxonFilter {ALL, HASPHOTO, HASSPECIMEN}

    private ArangoDatabase database;

    public OccurrenceReportArangoDriver(IFloraOn driver) {
        super(driver);
        this.database = (ArangoDatabase) driver.getDatabase();
    }

    @Override
    public int getNumberOfInventories(INodeKey userId, Date from, Date to) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        DateFormat df = Constants.dateFormatYMD.get();
        bindVars.put("user", userId.toString());
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));
//        System.out.println(new Gson().toJson(bindVars));
        try {
            return database.query(AQLOccurrenceQueries.getString("occurrencereportquery.1"), bindVars
                    , null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<TaxEnt> getTaxaWithTag(INodeKey userId, Date from, Date to, String territory, String tag, boolean withPhoto) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        DateFormat df = Constants.dateFormatYMD.get();
        bindVars.put("user", userId.toString());
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));
        bindVars.put("tag", tag);
        bindVars.put("@redlistcollection", "redlist_" + territory);
        try {
            return database.query(AQLOccurrenceQueries.getString(
                    withPhoto ? "occurrencereportquery.4" : "occurrencereportquery.3"), bindVars
                    , null, TaxEnt.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<StatisticPerTaxon> getTaxaWithTagCollected(INodeKey userId, Date from, Date to, String territory, String tag) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        DateFormat df = Constants.dateFormatYMD.get();
        bindVars.put("user", userId.toString());
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));
        bindVars.put("tag", tag);
        bindVars.put("@redlistcollection", "redlist_" + territory);
        try {
            return database.query(AQLOccurrenceQueries.getString("occurrencereportquery.5"), bindVars
                    , null, StatisticPerTaxon.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<StatisticPerTaxon> getTaxaWithTagNrRecords(INodeKey userId, Date from, Date to, String territory, String tag) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        DateFormat df = Constants.dateFormatYMD.get();
        bindVars.put("user", userId.toString());
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));
        bindVars.put("tag", tag);
        bindVars.put("@redlistcollection", "redlist_" + territory);
        try {
            return database.query(AQLOccurrenceQueries.getString("occurrencereportquery.6"), bindVars
                    , null, StatisticPerTaxon.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
