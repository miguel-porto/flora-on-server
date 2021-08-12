package pt.floraon.occurrences.dataproviders;

import jline.internal.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.IOccurrenceDriver;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class iNaturalistImporterJob  implements JobTask {
    private final User maintainer;
    private Integer progressCreated = 0, progressUpdated = 0;
    private iNaturalistDataProvider iNat;
    private iNaturalistFilter iNaturalistFilter;

    public iNaturalistImporterJob(User maintainer, String[] taxonNames, String[] identifiers, String project, String[] observers) {
        this.maintainer = maintainer;
        this.iNaturalistFilter = new iNaturalistFilter().withIdentifiers(identifiers).withObservers(observers)
                .withTaxonNames(taxonNames).withProjectId(project);
    }

    @Override
    public String getState() {
        if(iNat == null)
            return "Starting...";
        else {
            if(iNat.size() < 0)
                return "Fetching iNaturalist URL...";
            else
                return "Importing occurrence " + (progressCreated + progressUpdated) + " of " + iNat.size() + ". " + progressCreated + " created, " + progressUpdated + " updated.";
        }
    }

    @Override
    public String getDescription() {
        return "iNaturalist occurrence importer";
    }

    @Override
    public User getOwner() {
        return maintainer;
    }

    private void iterateiNaturalistDataProvider(iNaturalistDataProvider iNat, Map<String, String> userMap, IFloraOn driver) throws FloraOnException {
        final IOccurrenceDriver od = driver.getOccurrenceDriver();
        for (Inventory inv : iNat) {
            System.out.println(inv.getObservers()[0] + " | " + inv._getCoordinates() + " | " + inv.getPubNotes() + " | " + inv._getDate());
            inv.setMaintainer(maintainer.getID());
            String[] obs = inv.getObservers();
            String tmp;
            if ((tmp = userMap.get(obs[0])) != null)
                inv.setObservers(new String[]{tmp});
            else {
                User user = new User();
                user.setiNaturalistUserName(obs[0]);
                user.setName(StringUtils.isStringEmpty(obs[1]) ? obs[0] : obs[1]);
                String id = driver.getAdministration().createUser(user).getID();
                user.setID(id);
                Log.info("NEW USER: " + obs[1] + " " + obs[0] + " " + id);
                userMap.put(user.getiNaturalistUserName(), user.getID());
                inv.setObservers(new String[]{id});
            }

            od.matchTaxEntNames(inv, false, false, null);
            Inventory tmp1 = od.getOccurrenceByUuid(inv._getOccurrences().get(0).getUuid().toString());
            if (tmp1 == null) {
                inv.setNewRecord(true);
                od.createInventory(inv);
                progressCreated++;
            } else {    // update existing
                OBSERVED_IN occ = tmp1._getOccurrences().get(0);
                inv.setNewRecord(false);
                inv._getOccurrences().get(0).setDateInserted(occ.getDateInserted());
                if(StringUtils.isStringEmpty(inv._getOccurrences().get(0).getTaxEntMatch()) &&
                        !StringUtils.isStringEmpty(occ.getTaxEntMatch()))   // no match in the original, but matched in our DB
                    inv._getOccurrences().get(0).setTaxEntMatch(occ.getTaxEntMatch());  // so we keep our match
                od.updateOccurrence(inv._getOccurrences().get(0).getUuid().toString(), inv);
                progressUpdated++;
            }
        }
    }

    @Override
    public void run(IFloraOn driver) throws FloraOnException, IOException {
        // make a user map of all users
        List<User> allUsers = driver.getAdministration().getAllUsers(false);
        Map<String, String> userMap = new HashMap<>();
        for(User u : allUsers)
            userMap.put(u.getiNaturalistUserName(), u.getID());

//        iNat = new iNaturalistDataProvider(new iNaturalistFilter().withProjectId("flora-on").withTaxonNames("cistus libanotis,mandragora autumnalis").withObservers("mjcorreia"));// .withIdentifiers("mjcorreia,pmarques,AAAA"));

        if(StringUtils.isArrayEmpty(this.iNaturalistFilter.getTaxon_names())) {
            iNat = new iNaturalistDataProvider(this.iNaturalistFilter);
            iterateiNaturalistDataProvider(iNat, userMap, driver);
        } else {
            for (String taxonName : this.iNaturalistFilter.getTaxon_names()) {
                iNat = new iNaturalistDataProvider(new iNaturalistFilter(this.iNaturalistFilter).withTaxonNames(taxonName));
                iterateiNaturalistDataProvider(iNat, userMap, driver);
            }
        }
        Log.info("END fetch iNaturalist");
    }
}
