package pt.floraon.occurrences.arangodb;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.sun.istack.NotNull;
import pt.floraon.driver.*;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.IOccurrenceReportDriver;
import pt.floraon.occurrences.StatisticPerTaxon;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrence;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by miguel on 08-07-2017.
 */
public class OccurrenceReportArangoDriver extends GOccurrenceReportDriver implements IOccurrenceReportDriver {
    public enum TypeOfCollaboration {TEXTAUTHOR, ASSESSOR, REVIEWER}

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
    public Iterator<StatisticPerTaxon> getAllTaxa(INodeKey userId, Date from, Date to) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        DateFormat df = Constants.dateFormatYMD.get();
        bindVars.put("user", userId.toString());
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));
        try {
            return database.query(AQLOccurrenceQueries.getString("occurrencereportquery.1a"), bindVars
                    , null, StatisticPerTaxon.class);
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
    public Iterator<SimpleOccurrence> getOccurrencesWithTagCollected(INodeKey userId, Date from, Date to, String territory, String tag) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        DateFormat df = Constants.dateFormatYMD.get();
        bindVars.put("user", userId.toString());
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));
        bindVars.put("tag", tag);
        bindVars.put("@redlistcollection", "redlist_" + territory);
        try {
            return database.query(AQLOccurrenceQueries.getString("occurrencereportquery.8"), bindVars
                    , null, SimpleOccurrence.class);
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

    @Override
    public Iterator<StatisticPerTaxon> getTaxaWithTagEstimates(INodeKey userId, Date from, Date to, String territory, String tag) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        DateFormat df = Constants.dateFormatYMD.get();
        bindVars.put("user", userId.toString());
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));
        bindVars.put("tag", tag);
        bindVars.put("@redlistcollection", "redlist_" + territory);
        try {
            return database.query(AQLOccurrenceQueries.getString("occurrencereportquery.7"), bindVars
                    , null, StatisticPerTaxon.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<TaxEnt> getRedListSheetsIsCollaborator(INodeKey userId, String territory, @NotNull TypeOfCollaboration typeOfCollaboration) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("user", userId.toString());
        bindVars.put("@redlistcollection", "redlist_" + territory);
        if(typeOfCollaboration == null) return Collections.emptyIterator();
        try {
            switch (typeOfCollaboration) {
                case TEXTAUTHOR:
                    return database.query(AQLOccurrenceQueries.getString("reportquery.1"), bindVars, null, TaxEnt.class);

                case ASSESSOR:
                    return database.query(AQLOccurrenceQueries.getString("reportquery.1a"), bindVars, null, TaxEnt.class);

                case REVIEWER:
                    return database.query(AQLOccurrenceQueries.getString("reportquery.1b"), bindVars, null, TaxEnt.class);

                default: return Collections.emptyIterator();
            }
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
