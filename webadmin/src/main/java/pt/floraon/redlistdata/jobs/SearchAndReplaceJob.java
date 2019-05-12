package pt.floraon.redlistdata.jobs;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.fieldprocessing.FieldProcessor;
import pt.floraon.redlistdata.fieldprocessing.FieldVisitor;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class SearchAndReplaceJob implements JobTask, FieldVisitor<SafeHTMLString> {
    private String progress;
    private Map<String, String> replaceTable;
    private FieldProcessor<RedListDataEntity, SafeHTMLString> fieldProcessor;
    private Iterator<RedListDataEntity> rldeIt;

    public SearchAndReplaceJob(Iterator<RedListDataEntity> sheetIterator, Map<String, String> replaceTable) {
        this.rldeIt = sheetIterator;
        this.replaceTable = replaceTable;
    }

    @Override
    public void run(IFloraOn driver) throws FloraOnException, IOException {
        this.fieldProcessor = new FieldProcessor<>(this.rldeIt, SafeHTMLString.class, this, driver);
        Iterator<RedListDataEntity> it = this.fieldProcessor.iterator();
        while(it.hasNext()) {
            RedListDataEntity rlde = it.next();
            progress = rlde.getTaxEnt().getNameWithAnnotationOnly(false);
            driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rlde.getID()), rlde, false, RedListDataEntity.class);
        }

    }

    @Override
    public String getState() {
        return "Processing " + progress;
    }

    @Override
    public String getDescription() {
        return "Search and replace";
    }

    @Override
    public User getOwner() {
        return null;
    }

    @Override
    public void process(Object bean, String propertyName, SafeHTMLString value) {
        // iterate through replacement table
        for(Map.Entry<String, String> e : this.replaceTable.entrySet()) {
            value.replaceSubString(e.getKey(), e.getValue());
        }
    }
}
