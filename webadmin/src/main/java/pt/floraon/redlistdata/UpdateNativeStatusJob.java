package pt.floraon.redlistdata;

import jline.internal.Log;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 14-12-2016.
 */
public class UpdateNativeStatusJob implements JobTask {
    private int n = 0, total;
    private String territory;

    UpdateNativeStatusJob(String territory) {
        this.territory = territory;
    }

    @Override
    public void run(IFloraOn driver) throws FloraOnException, IOException {
        Log.info("Updating red list dataset for " + territory);
        List<RedListDataEntity> rldel = driver.getRedListData().getAllRedListData(territory, false);
        total = rldel.size();
        Map<String, Object> v = new HashMap<>();

        for(RedListDataEntity rlde : rldel) {
            InferredStatus is = driver.wrapTaxEnt(driver.asNodeKey(rlde.getTaxEntID())).getInferredNativeStatus(territory);
//            rlde.setInferredStatus(is);
//            INodeKey nk = driver.asNodeKey(rlde.getID());
//            driver.getRedListData().updateRedListDataEntity(territory, nk, rlde, false);
            v.clear();
            v.put("inferredStatus", is);
            driver.getRedListData().updateRedListDataEntities(territory, new String[] {rlde.getTaxEntID()}, v);
            n++;
        }

    }

    @Override
    public String getState() {
        return String.format("%d / %d done.", n, total);
    }
}
