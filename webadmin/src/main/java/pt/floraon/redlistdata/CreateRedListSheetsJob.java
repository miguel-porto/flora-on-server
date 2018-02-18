package pt.floraon.redlistdata;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.io.IOException;
import java.util.List;

/**
 * Computes the native status of all taxa existing in given territory and stores in the collection redlist_(territory)
 * Created by miguel on 10-11-2016.
 */
public class CreateRedListSheetsJob implements JobTask {
    private int n = 0, total;
    private String territory;

    CreateRedListSheetsJob(String territory) {
        this.territory = territory;
    }

    @Override
    public void run(IFloraOn driver) throws FloraOnException, IOException {
        System.out.println("Creating red list dataset for " + territory);
        List<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, territory, null, null);
        total = taxEntList.size();
        RedListDataEntity rlde;

        for(TaxEnt te1 : taxEntList) {
//            System.out.println("Creating "+te1.getID());

            rlde = new RedListDataEntity(te1.getID(), driver.wrapTaxEnt(driver.asNodeKey(te1.getID())).getInferredNativeStatus(territory));
//            System.out.println(new Gson().toJson(rlde));
            driver.getRedListData().createRedListDataEntity(territory, rlde);
//            System.out.println(te1.getFullName()+": "+ rlde.getInferredStatus().getStatusSummary());
            n++;
        }

    }

    @Override
    public String getState() {
        return String.format("%d / %d done.", n, total);
    }

    @Override
    public String getDescription() {
        return "Compute native status";
    }

    @Override
    public User getOwner() {
        return null;
    }
}
