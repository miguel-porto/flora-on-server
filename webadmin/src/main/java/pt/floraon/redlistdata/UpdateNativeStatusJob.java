package pt.floraon.redlistdata;

import com.arangodb.internal.velocypack.VPackSerializers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jline.internal.Log;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.io.IOException;
import java.util.List;

/**
 * Created by miguel on 14-12-2016.
 */
public class UpdateNativeStatusJob implements JobTask {
    private int n = 0, total;
    @Override
    public void run(IFloraOn driver, Object options) throws FloraOnException, IOException {
        String territory = (String) options;
        Log.info("Updating red list dataset for " + territory);
        List<RedListDataEntity> rldel = driver.getRedListData().getAllRedListTaxa(territory, false);
        total = rldel.size();

        for(RedListDataEntity rlde : rldel) {
            InferredStatus is = driver.wrapTaxEnt(driver.asNodeKey(rlde.getTaxEntID())).getInferredNativeStatus(territory);
            rlde.setInferredStatus(is);
            INodeKey nk = driver.asNodeKey(rlde.getID());
            driver.getRedListData().updateRedListDataEntity(territory, nk, rlde, true);
            n++;
        }

    }

    @Override
    public String getState() {
        return String.format("%d / %d done.", n, total);
    }
}
