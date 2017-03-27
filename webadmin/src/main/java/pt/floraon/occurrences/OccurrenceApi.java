package pt.floraon.occurrences;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jline.internal.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by miguel on 23-03-2017.
 */
@MultipartConfig
@WebServlet("/occurrences/api/*")
public class OccurrenceApi extends FloraOnServlet {
    @Override
    public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = getPathIteratorAfter("api");
        Gson gs = new GsonBuilder().setPrettyPrinting().create();
        String fileName;
        User user = getUser();
        if(user.isGuest()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        switch (path.next()) {
            case "savetable":
                fileName = getParameterAsString("file");
                errorIfAnyNull(fileName);

                boolean main = getParameterAsBoolean("mainobserver", false);
                user = refreshUser();
                if(!user.getUploadedTables().contains(fileName)) throw new FloraOnException("File not found.");

                Log.info("Reading " + fileName);
                InventoryList invList;
                try {
                    invList = Common.readInventoryListFromFile(fileName);
                } catch (ClassNotFoundException e) {
                    throw new FloraOnException(e.getMessage());
                }

                for(Inventory inv : invList) {
                    if(main) {
                        if(StringUtils.isArrayEmpty(inv.getObservers()))
                            inv.setObservers(new String[] {getUser().getID()});
                    }

                    driver.getOccurrenceDriver().createInventory(inv);
                }

                driver.getOccurrenceDriver().discardUploadedTable(driver.asNodeKey(getUser().getID()), fileName);
                success("Ok");

                break;

            case "discardtable":
                fileName = getParameterAsString("file");
                errorIfAnyNull(fileName);
                success(driver.getOccurrenceDriver().discardUploadedTable(driver.asNodeKey(getUser().getID()), fileName) ? "Ok" : "Nothing deleted.");
                break;

            case "addoccurrences":
                // NOTE: this expects HTML field names of the form xxxxxx_latitude, xxxxxx_taxa, etc. (6 uid digits for each row)
                Enumeration<String> en = request.getParameterNames();
                Multimap<String, String> map = ArrayListMultimap.create();
                while(en.hasMoreElements()) {
                    String name = en.nextElement();
                    String id = name.substring(0, 6);
                    map.put(id, name);
                }

                InventoryList inventories = new InventoryList();
                Inventory inv;
                OccurrenceParser op = new OccurrenceParser(driver);
                Map<String, Map<String, String>> values = new HashMap<>();
                for(Map.Entry<String, Collection<String>> ent : map.asMap().entrySet()) {
                    // new table line means new inventory
                    inventories.add(inv = new Inventory());
                    for(String name : ent.getValue()) {
                        String field = name.substring(7);
                        op.parseField(getParameterAsString(name), field, inv);
                    }
                }

                driver.getOccurrenceDriver().matchTaxEntNames(inventories);
                System.out.println(gs.toJson(inventories));

                int count = 0;
                for(Inventory inv1 : inventories) {
                    if(StringUtils.isArrayEmpty(inv1.getObservers()))
                        inv1.setObservers(new String[] {getUser().getID()});

                    driver.getOccurrenceDriver().createInventory(inv1);
                    count++;
                }
                success(count + " inventories saved.");
                break;
        }
    }
}
