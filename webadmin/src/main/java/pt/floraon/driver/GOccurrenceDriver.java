package pt.floraon.driver;

import jline.internal.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeWorker;
import pt.floraon.driver.interfaces.IOccurrenceDriver;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.occurrences.TaxonomicChange;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.occurrences.fields.FieldReflection;
import pt.floraon.occurrences.fields.parsers.EnumParser;
import pt.floraon.occurrences.iterators.OccurrenceIterator;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.TaxonName;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 26-03-2017.
 */
public abstract class GOccurrenceDriver extends BaseFloraOnDriver implements IOccurrenceDriver {
//    private static Pattern filterPattern = Pattern.compile("((?<key>[a-zA-Z]+): *(?<value>[\\wçãõáàâéêíóôú/?.,;<>*-]+))");
    private static Pattern filterPattern =
        Pattern.compile("((?<key>[a-zA-Z]+): *(?<value>[\\wçãõáàâéêíóôú/?ºª&.,;<>* ()-]+?)(?= +[a-zA-Z]+:| *$))");
    private static List<String> infraRanks = Arrays.asList("subsp.", "var.", "f.", "ssp.", "subvar.", "forma");

    public GOccurrenceDriver(IFloraOn driver) {
        super(driver);
    }

    @Override
    public void matchTaxEntNames(Inventory inventory, boolean createNew, boolean doMatch, InventoryList inventories) throws FloraOnException {
        INodeWorker nwd = driver.getNodeWorkerDriver();
        MutableBoolean ask = new MutableBoolean(false);
        for(OBSERVED_IN oi : inventory.getUnmatchedOccurrences()) {
            TaxEnt te, te1;
            List<TaxEnt> matched;
            Log.info("Verbose name: "+ oi.getVerbTaxon());
            if(oi.getVerbTaxon() == null) continue;

            if(oi.getVerbTaxon().trim().equals("")) {
//                Log.info("    Empty name, clearing");
//                if(inventories != null) inventories.addNoMatch(oi);
                oi.setTaxEntMatch("");
                continue;
            }

            try {
                te = new TaxEnt(new TaxonName(oi.getVerbTaxon()));
//                te = TaxEnt.parse2(oi.getVerbTaxon());      // this allows uninomials
            } catch (FloraOnException e) {  // could not even parse the name
                if(inventories != null)
//                    inventories.addQuestion(oi.getVerbTaxon(), oi.getUuid(), null);
                    inventories.addParseError(oi.getVerbTaxon());
                Log.warn(oi.getVerbTaxon() + ": " + e.getMessage());
                oi.setTaxEntMatch("");
                continue;
            }
//            Log.info("    Parsed name: "+ te.getFullName(false));
            matched = nwd.getTaxEnt(te, ask, false);
            if(matched.size() > 1) Log.info("    Matched size: "+ matched.size());
            if(matched.size() == 0) {
                // let's check if this is a trinomial without infrarank
                String tmp = oi.getVerbTaxon().replaceAll(" +", " ").trim();
                String[] splitted = tmp.split(" ");
                //Log.info("    Splitted length of " + tmp + ":" + splitted.length);
                if(splitted.length == 3) {
                    StringBuilder tmp1 = new StringBuilder();
                    for (String iR : infraRanks) {
                        tmp1.setLength(0);
                        tmp1.append(splitted[0]).append(" ").append(splitted[1]).append(" ")
                                .append(iR).append(" ").append(splitted[2]);
                        Log.info("    Checking trinomial " + tmp1.toString());
                        try {
                            te = new TaxEnt(new TaxonName(tmp1.toString()));
                            matched = nwd.getTaxEnt(te, ask, false);
//                            Log.info("    Matched size: " + matched.size());
                            if(matched.size() == 0) {
                                te = null;
                                continue;
                            }
                            break;
                        } catch (FloraOnException ignored) {
                        }
                    }
                }
            }

            switch(matched.size()) {
            case 0:
                if (createNew) {
                    te1 = nwd.createTaxEntFromTaxEnt(te);
                    Log.warn("    No match, created new taxon");
                    if(inventories != null) inventories.addNoMatch(oi);
                    oi.setTaxEntMatch(te1.getID());
                } else {
                    Log.warn("    No match, do you want to add new taxon?");
                    if(inventories != null)
                        inventories.addQuestion(oi.getVerbTaxon(), oi.getUuid(), null);
                        //inventories.addNoMatch(oi);
                    oi.setTaxEntMatch("");
                }
                break;

            default:
                if(!ask.booleanValue()) {
                    Log.info("    Matched name: " + matched.get(0).getFullName(false), " -- ", matched.get(0).getID());
                    oi.setTaxEntMatch(matched.get(0).getID());
                    if(doMatch && inventories != null) {
                        Map<String, TaxonomicChange> tmp1 = new HashMap<>();
                        tmp1.put(oi.getVerbTaxon(), new TaxonomicChange(matched.get(0).getID(), oi.getUuid().toString(), null));
                        replaceTaxEntMatch(tmp1);
                        inventories.getVerboseWarnings().add("Automatically matched " + oi.getVerbTaxon() + " to " + matched.get(0).getID());
                        //inventories.addQuestion(oi.getVerbTaxon(), oi.getUuid(), matched.get(0));
                    }
                } else {
                    if(matched.size() == 0 && inventories != null)
                        inventories.addQuestion(oi.getVerbTaxon(), oi.getUuid(), null);
                    else {
                        for (TaxEnt tmp : matched) {
                            if (inventories != null)
                                inventories.addQuestion(oi.getVerbTaxon(), oi.getUuid(), tmp);
                        }
                    }
                    oi.setTaxEntMatch("");
                }
                break;
            }
        }
    }

    @Override
    public void matchTaxEntNames(InventoryList inventories, boolean createNew, boolean doMatch) throws FloraOnException {
        for(Inventory i : inventories)
            matchTaxEntNames(i, createNew, doMatch, inventories);
    }

    @Override
    public InventoryList matchTaxEntNames(Iterator<Inventory> inventories, boolean createNew, boolean doMatch) throws FloraOnException {
        InventoryList inventoryList = new InventoryList();
        while(inventories.hasNext())
            matchTaxEntNames(inventories.next(), createNew, doMatch, inventoryList);
        return inventoryList;
    }

    @Override
    public Map<String, String> parseFilterExpression(String filterText) {
        if(pt.floraon.driver.utils.StringUtils.isStringEmpty(filterText))
            return new HashMap<>();

        Map<String, String> out = new HashMap<>();

        Matcher mat = filterPattern.matcher(filterText);

        while(mat.find()) {
//            System.out.println(mat.group("key") + ": "+mat.group("value"));
            if(!pt.floraon.driver.utils.StringUtils.isStringEmpty(mat.group("value")))
                out.put(mat.group("key").toLowerCase(), mat.group("value"));
        }
        String na = mat.replaceAll("").trim();
        if(!pt.floraon.driver.utils.StringUtils.isStringEmpty(na))
            out.put("NA", na);
        return out;
    }

    @Override
    public Iterator<Occurrence> getFilteredOccurrences(OccurrenceFilter filter) throws DatabaseException {
        Iterator<Occurrence> tmp =
                this.driver.getOccurrenceDriver().getOccurrencesOfMaintainer(null, null, true
                        , null, null);
        return new OccurrenceIterator(tmp, new OccurrenceFilter[] {filter});
    }

    protected String buildSortOrderExpression(AbstractMap.SimpleEntry<String, Boolean> orderField) {
        StringBuilder sortFields = new StringBuilder();

        if (orderField == null || orderField.getKey() == null || FieldReflection.findField(orderField.getKey()) == null)
            sortFields.append("osort.dateInserted DESC");
        else if(orderField.getKey().equals("coordinates")) {
            sortFields.append("(i.latitude == null ? osort.observationLatitude : i.latitude)")
                    .append(orderField.getValue() ? " DESC" : "");
        } else {
            String[] dbOrderFields = FieldReflection.getDatabaseFields(orderField.getKey());
            String direction = orderField.getValue() ? " DESC" : "";
            for (String f : dbOrderFields) {
                String io = FieldReflection.isInventoryField(f) ? "i." : "osort.";
                sortFields.append(io);
                Class<? extends pt.floraon.driver.parsers.FieldParser> fp = FieldReflection.getFieldParser(f);
                if(fp != null && fp.equals(EnumParser.class)) {
                    sortFields.append(f).append(" == 'NULL' ? '' : ").append(io).append(f).append(direction).append(", ");
                } else
                    sortFields.append(f).append(direction).append(", ");
            }
            sortFields.setLength(sortFields.length() - 2);
        }
        return sortFields.toString();
    }
}
