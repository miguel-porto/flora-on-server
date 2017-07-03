package pt.floraon.occurrences;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jline.internal.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.fieldparsers.UserListParser;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 23-03-2017.
 */
@MultipartConfig
@WebServlet("/occurrences/api/*")
public class OccurrenceApi extends FloraOnServlet {
    @Override
    public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = thisRequest.getPathIteratorAfter("api");
        Gson gs = new GsonBuilder().setPrettyPrinting().create();
        String fileName;
        User user = thisRequest.getUser();
        if(user.isGuest()) {
            thisRequest.response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String option = path.next();

        switch (option) {
            case "fixtaxonomicissues":
                driver.getOccurrenceDriver().replaceTaxEntMatch(buildTaxonomicIssues(thisRequest
                        , thisRequest.getUser().isAdministrator() ? null : thisRequest.getUser()));
                thisRequest.success("Ok");
                break;

            case "savetable":
                fileName = thisRequest.getParameterAsString("file");
                errorIfAnyNull(fileName);
                user = thisRequest.refreshUser();
                if(!user.getUploadedTables().contains(fileName)) throw new FloraOnException("File not found.");

                Log.info("Reading " + fileName);
                InventoryList invList;
                try {
                    invList = Common.readInventoryListFromFile(fileName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new FloraOnException(e.getMessage());
                }

                for(Inventory inv : invList)
                    driver.getOccurrenceDriver().createInventory(inv);

                Map<String, TaxonomicChange> rep = buildTaxonomicIssues(thisRequest, user);
//                System.out.println(gs.toJson(rep));
                driver.getOccurrenceDriver().replaceTaxEntMatch(rep);

                driver.getOccurrenceDriver().discardUploadedTable(driver.asNodeKey(thisRequest.getUser().getID()), fileName);
                thisRequest.success("Ok");
                break;

            case "discardtable":
                fileName = thisRequest.getParameterAsString("file");
                errorIfAnyNull(fileName);
                thisRequest.success(driver.getOccurrenceDriver().discardUploadedTable(driver.asNodeKey(thisRequest.getUser().getID()), fileName) ? "Ok" : "Nothing deleted.");
                break;

            case "addoccurrences":
            case "updateoccurrences":
            case "updateinventory":
                // NOTE: this expects HTML field names of the form xxxxxx_yyyyyy_latitude, xxxxxx_yyyyyy_taxa, etc. or just xxxxxx_taxa, etc.
                // First form is for inventories - 6 uid digits for grouping, other 6 for table rows
                // Second form for occurrences
                Pattern ids = Pattern.compile("^(?<id1>[a-zA-Z0-9]{6})_((?<id2>[a-zA-Z0-9]{6})_)?(?<name>[a-zA-Z0-9]+)$");
                Enumeration<String> en = thisRequest.request.getParameterNames();
                Multimap<String, String> grp = ArrayListMultimap.create();
                Multimap<String, String> map = ArrayListMultimap.create();
                Multimap<String, String> invfields = ArrayListMultimap.create();
                while(en.hasMoreElements()) {
                    String name = en.nextElement();
                    Matcher mat = ids.matcher(name);
                    if(!mat.find()) continue;
                    String id1 = mat.group("id1");
                    String id2 = mat.group("id2");
                    if(id2 != null) {
                        grp.put(id1, id2);
                        map.put(id2, name);
                    } else
                        invfields.put(id1, name);

                }

/*
                System.out.println(gs.toJson(grp.asMap()));
                System.out.println(gs.toJson(map.asMap()));
                System.out.println(gs.toJson(invfields.asMap()));
*/

                InventoryList inventories = new InventoryList();
                Inventory inv;
                OccurrenceParser op = new OccurrenceParser(driver);

                boolean createTaxa = thisRequest.getParameterAsBoolean("createTaxa", false);
                if(thisRequest.getParameterAsBoolean("createUsers", false)) {
                    op.registerParser("observers", new UserListParser(op.getUserMap(), driver, true));
                    op.registerParser("collectors", new UserListParser(op.getUserMap(), driver, true));
                    op.registerParser("determiners", new UserListParser(op.getUserMap(), driver, true));
                }

                Set<String> tmpset = new HashSet<>(grp.keySet());
                tmpset.addAll(invfields.keySet());
                for(String invid : tmpset) {    // each inventory
                    inventories.add(inv = new Inventory());
                    Map<String, String> keyValues = new HashMap<>();

                    for(String name : invfields.get(invid)) {   // set the inventory fields
                        String field = name.substring(7);
                        System.out.println(name + ": "+thisRequest.getParameterAsString(name));
                        keyValues.put(field, thisRequest.getParameterAsString(name));
                    }
                    op.parseFields(keyValues, inv);     // feed in inventory fields

                    for(String id2 : new HashSet<>(grp.get(invid))) {    // go through all occurrence fields this inventory
                        System.out.println("ID2: " + id2);
                        Inventory tmp = new Inventory();
                        keyValues = new HashMap<>();
                        for(String name : map.get(id2)) {   // this is one occurrence (one table row)
                            String field = name.substring(14);
                            System.out.println(name + ": "+thisRequest.getParameterAsString(name));
                            keyValues.put(field, thisRequest.getParameterAsString(name));
                        }

                        op.parseFields(keyValues, tmp);
                        inv.getUnmatchedOccurrences().addAll(tmp.getUnmatchedOccurrences());
                    }

                    if(option.equals("addoccurrences") && inv.getUnmatchedOccurrences().size() == 0)
                        inv.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
                }

                driver.getOccurrenceDriver().matchTaxEntNames(inventories, createTaxa, false);
                System.out.println("************ REQUESTED BEANS:");
                System.out.println(gs.toJson(inventories));

                if(option.equals("addoccurrences")) {
                    int count = 0;
                    boolean main1 = thisRequest.getParameterAsBoolean("mainobserver", false);
                    if(inventories.size() == 0)
                        thisRequest.error("Empty inventories, none saved.");
                    else {
                        for (Inventory inv1 : inventories) {
                            if(main1) {
                                Set<String> obs = new LinkedHashSet<>();
                                obs.add(thisRequest.getUser().getID());
                                obs.addAll(Arrays.asList(inv1.getObservers()));
                                inv1.setObservers(obs.toArray(new String[obs.size()]));
                            }/* else {
                                if (StringUtils.isArrayEmpty(inv1.getObservers()))
                                    inv1.setObservers(new String[]{getUser().getID()});
                            }*/
                            inv1.setMaintainer(thisRequest.getUser().getID());
                            driver.getOccurrenceDriver().createInventory(inv1);
                            count++;
                        }
                        thisRequest.success(count + " inventories saved.", true);
                    }
                }
// FIXME update empty fields
                if(option.equals("updateoccurrences") || option.equals("updateinventory")) {
                    for(Inventory inv1 : inventories) {
//                        Log.warn(inv1.getID());
                        driver.getOccurrenceDriver().updateInventory(inv1);
                    }
                    thisRequest.success("ok");
                }
                break;

            case "deleteoccurrences":
                driver.getOccurrenceDriver().deleteInventoriesOrOccurrences(thisRequest.request.getParameterValues("inventoryId")
                        , thisRequest.request.getParameterValues("occurrenceUuid"));
                thisRequest.success("Ok");
                break;

            case "mergeoccurrences":
                // merges occurrences into one inventory. this discards the coordinates of the merged inventory and assigns coordinates
                // to each observation separately
                Iterator<Inventory> it = driver.getOccurrenceDriver().getOccurrencesByUuid(driver.asNodeKey(thisRequest.getUser().getID())
                        , thisRequest.request.getParameterValues("occurrenceUuid"));

                List<Inventory> tmp = Lists.newArrayList(it);
                if(tmp.size() == 0) {
                    thisRequest.success("Ok");
                    break;
                }

                for(Inventory tmp1 : tmp) {
                    for(OBSERVED_IN occ : tmp1._getOccurrences()) {
                        if(occ.getObservationLatitude() == null)
                            occ.setObservationLatitude(tmp1.getLatitude());
                        if(occ.getObservationLongitude() == null)
                            occ.setObservationLongitude(tmp1.getLongitude());
                    }
                }

                Inventory merged;
                try {
                    // we ignore the field that holds the occurrences and those that are replicated in the OBSERVED_BY
                    merged = BeanUtils.mergeBeans(Inventory.class
                            , Arrays.asList("unmatchedOccurrences", "latitude", "longitude", "ID", "key", "databaseId", "code", "elevation"), tmp.toArray(new Inventory[tmp.size()]));
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                    e.printStackTrace();
                    throw new FloraOnException(e.getMessage());
                } catch (FloraOnException e) {
                    throw new FloraOnException(Messages.getString("error.3"));
                }

                // assemble all species found in these inventories into the merged one, and set the codes from respective inventories
                List<OBSERVED_IN> occ = new ArrayList<>();
                OBSERVED_IN tmpo;
                for (Inventory inventory : tmp) {
                    if(inventory._getOccurrences().size() == 0) {
                        tmpo = new OBSERVED_IN(true);
                        tmpo.setGpsCode(inventory.getCode());
                        occ.add(tmpo);
                    } else {
                        for (OBSERVED_IN noi : inventory._getOccurrences()) {
                            if (noi.getGpsCode() == null) noi.setGpsCode(inventory.getCode());
                        }
                        occ.addAll(inventory._getOccurrences());
                    }
                }
                merged.setUnmatchedOccurrences(occ);
                System.out.println(gs.toJson(merged));
                driver.getOccurrenceDriver().createInventory(merged);

                for(Inventory tmp1 : tmp) {
                    driver.getOccurrenceDriver().deleteInventoriesOrOccurrences(
                            new String[] {tmp1.getID()}, new String[] {tmp1._getOccurrences().size() == 0 ? ""
                                    : tmp1._getOccurrences().get(0).getUuid().toString()});
                }
/*

                Set<String> toDelete = new HashSet<>();
                for(Inventory tmp1 : tmp)
                    toDelete.add(tmp1.getID());

                for(String td : toDelete)
                    driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(td));
*/

                thisRequest.success("Ok");
                break;

            case "matchtaxa":
                driver.getOccurrenceDriver().matchTaxEntNames(driver.getOccurrenceDriver().getUnmatchedOccurrences(), true, true);

                break;
        }
    }

    /**
     * Convert the HTML fields to taxonomic changes
     * @param thisRequest
     * @return
     */
    private Map<String, TaxonomicChange> buildTaxonomicIssues(ThisRequest thisRequest, User user) {
        Map<String, String[]> params = thisRequest.request.getParameterMap();
        Map<Integer, String[]> answerKeys = new HashMap<>();
        Map<String, TaxonomicChange> taxEntMatches = new HashMap<>();
        for(Map.Entry<String, String[]> p : params.entrySet()) {
            if(p.getKey().startsWith("question_") && p.getKey().endsWith("_key"))
                answerKeys.put(Integer.parseInt(p.getKey().substring(9, p.getKey().length() - 4)), new String[] {p.getValue()[0], null});
        }
        for(Map.Entry<String, String[]> p : params.entrySet()) {
            if (p.getKey().startsWith("question_") && p.getKey().endsWith("_uuids")) {
                String[] tmp = answerKeys.get(Integer.parseInt(p.getKey().substring(9, p.getKey().length() - 6)));
                tmp[1] = p.getValue()[0];
            }
//                answerUUIDs.put(Integer.parseInt(p.getKey().substring(9, p.getKey().length() - 6)), p.getValue()[0]);
        }

        for(Map.Entry<String, String[]> p : params.entrySet()) {
            if(p.getKey().startsWith("question_") && !p.getKey().endsWith("_key") && !p.getKey().endsWith("_uuids")) {
                Integer q = Integer.parseInt(p.getKey().substring(9));
                taxEntMatches.put(answerKeys.get(q)[0], new TaxonomicChange(p.getValue()[0], answerKeys.get(q)[1]
                        , user == null ? null : user.getID()));
            }
        }

        return taxEntMatches;
    }
}
