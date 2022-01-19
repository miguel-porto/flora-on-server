package pt.floraon.redlistdata.jobs;

import org.apache.commons.lang.mutable.MutableInt;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.jobs.JobFileDownload;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.occurrences.BasicOccurrenceFilter;
import pt.floraon.redlistdata.occurrences.OccurrenceProcessor;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Compiles a table of taxa inside a polygon, with taxa-level red list info.
 */
public class DownloadTaxaInPolygonJob implements JobFileDownload {
    private final String territory, polygonWKT;
    private final PolygonTheme clippingPolygon;
    private int curSpeciesI = 0;
    private boolean finalPhase = false;
    private String curSpeciesName = "";
    private final User owner;

    public DownloadTaxaInPolygonJob(String territory, String polygonWKT, PolygonTheme clippingPolygon, User owner) {
        this.territory = territory;
        this.polygonWKT = polygonWKT;
        this.clippingPolygon = clippingPolygon;
        this.owner = owner;
    }

    @Override
    public Charset getCharset() {
        return Charset.forName("windows-1252");
    }

    @Override
    public void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException {
        Map<INodeKey, Object> taxaSet = new HashMap<>();

        List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();

        Iterator<TaxEnt> taxEntIterator = driver.getRedListData().getAllRedListTaxa(territory);
        TaxEnt taxEnt;

        // this is to prevent the database cursor timeout
        List<TaxEnt> te = new ArrayList<>();
        while(taxEntIterator.hasNext())
            te.add(taxEntIterator.next());
        taxEntIterator = te.iterator();

        while(taxEntIterator.hasNext()) {
            taxEnt = taxEntIterator.next();
//            if(taxEnt.getOldId() == null || taxEnt.getOldId() != 183) continue;
            curSpeciesI++;
            curSpeciesName = taxEnt.getName();
            for (SimpleOccurrenceDataProvider edp : sodps)
                edp.executeOccurrenceQuery(taxEnt);

            OccurrenceProcessor op = OccurrenceProcessor.iterableOf(sodps
                    , BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory, polygonWKT));

            for (Occurrence occ : op) {
                // this adds the taxa and records the most recent year of observation of this taxon, in the polygon
                INodeKey tmpKey = driver.asNodeKey(taxEnt.getID());
                if (taxaSet.containsKey(tmpKey)) {
                    if (occ.getYear() != null) {
                        Integer tmp1 = ((MutableInt) taxaSet.get(tmpKey)).toInteger();
                        if (tmp1 == null)
                            ((MutableInt) taxaSet.get(tmpKey)).setValue(occ.getYear());
                        else {
                            if (occ.getYear() > ((MutableInt) taxaSet.get(tmpKey)).intValue())
                                ((MutableInt) taxaSet.get(tmpKey)).setValue(occ.getYear());
                        }
                    }
                } else
                    taxaSet.put(tmpKey, new MutableInt(occ.getYear()));
            }
        }
        finalPhase = true;
        new ComputeAOOEOOJobWithInfo(territory, 2000
                // NOTE this filter is for computing AOO & EOO only!
                , BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory, clippingPolygon)
                , null, taxaSet, owner).run(driver, out);
    }

    @Override
    public String getState() {
        return finalPhase
                ? "2/2: Processing computations"
                : "1/2: Processing occurrences of species " + curSpeciesI + " (" + curSpeciesName + ")";
    }

    @Override
    public String getDescription() {
        return "List of taxa within polygon";
    }

    @Override
    public User getOwner() {
        return owner;
    }
}
