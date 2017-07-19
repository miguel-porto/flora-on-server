package pt.floraon.driver;

import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.StatisticPerTaxon;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by miguel on 08-07-2017.
 */
public interface IOccurrenceReportDriver {
    int getNumberOfInventories(INodeKey userId, Date from, Date to) throws DatabaseException;
    int getNumberOfTaxaWithTag(INodeKey userId, Date from, Date to, String territory, String tag) throws DatabaseException;
    Iterator<TaxEnt> getTaxaWithTag(INodeKey userId, Date from, Date to, String territory, String tag, boolean withPhoto) throws DatabaseException;
    Iterator<StatisticPerTaxon> getTaxaWithTagCollected(INodeKey userId, Date from, Date to, String territory, String tag) throws DatabaseException;
    Iterator<StatisticPerTaxon> getTaxaWithTagNrRecords(INodeKey userId, Date from, Date to, String territory, String tag) throws DatabaseException;
    Map<String, Integer> getListOfUTMSquaresWithOccurrences(INodeKey userId, Date from, Date to, long sizeOfSquare) throws DatabaseException;
    Map<String, Integer> getListOfPolygonsWithOccurrences(INodeKey userId, Date from, Date to, PolygonTheme polygonTheme) throws DatabaseException;
}
