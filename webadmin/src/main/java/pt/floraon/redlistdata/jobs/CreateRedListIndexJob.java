package pt.floraon.redlistdata.jobs;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Computes the native status of all taxa existing in given territory and stores in the collection redlist_(territory)
 * Created by miguel on 10-11-2016.
 */
public class CreateRedListIndexJob implements JobTask {
    private int n = 0;
    private String territory;

    public CreateRedListIndexJob(String territory) {
        this.territory = territory;
    }

    @Override
    public void run(IFloraOn driver) throws FloraOnException, IOException {
        System.out.println("Creating red list dataset for " + territory);
        Iterator<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, territory, null, null);
        RedListDataEntity rlde;
        TaxEnt te1;

        while(taxEntList.hasNext()) {
            te1 = taxEntList.next();
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
        return String.format("%d done.", n);
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
