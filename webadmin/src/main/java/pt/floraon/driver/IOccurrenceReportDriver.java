package pt.floraon.driver;

import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by miguel on 08-07-2017.
 */
public interface IOccurrenceReportDriver {
    int getNumberOfInventories(INodeKey userId, Date from, Date to) throws DatabaseException;
    int getNumberOfTaxaWithTag(INodeKey userId, Date from, Date to, String territory, String tag) throws DatabaseException;
    Iterator<TaxEnt> getTaxaWithTag(INodeKey userId, Date from, Date to, String territory, String tag, boolean withPhoto) throws DatabaseException;
}
