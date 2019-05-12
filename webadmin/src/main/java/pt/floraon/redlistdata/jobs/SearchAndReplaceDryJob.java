package pt.floraon.redlistdata.jobs;

import org.apache.commons.io.IOUtils;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.fieldprocessing.FieldProcessor;
import pt.floraon.redlistdata.fieldprocessing.FieldVisitor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A job that lists all substrings that will be replaced, in all text fields, if replacement is requested.
 */
public class SearchAndReplaceDryJob implements JobTask, FieldVisitor<SafeHTMLString> {
    private Map<String, String> replaceTable;
    private File results;
    private OutputStream outputStream;
    private Iterator<RedListDataEntity> rldeIt;
    private FieldProcessor<RedListDataEntity, SafeHTMLString> fieldProcessor;
    private Map<String, Set<String>> resultMap = new HashMap<>();

    public SearchAndReplaceDryJob(Iterator<RedListDataEntity> sheetIterator, Map<String, String> replaceTable) {
        this.rldeIt = sheetIterator;
        this.replaceTable = replaceTable;
        try {
            this.results = File.createTempFile("floraon_", null);
            this.outputStream = new FileOutputStream(this.results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(IFloraOn driver) {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        this.fieldProcessor = new FieldProcessor<>(this.rldeIt, SafeHTMLString.class, this, driver);
        this.fieldProcessor.run();

        // hey we output results as HTML, sorry, no time for better solution now.
        out.println("<table><tr><th>Entry</th><th>Matches</th></tr>");
        for(Map.Entry<String, Set<String>> row : this.resultMap.entrySet()) {
            out.println("<tr><td>" + row.getKey() + "</td><td><table>");
            for(String s : row.getValue()) {
                SafeHTMLString s1 = new SafeHTMLString(s);
                s1.replaceSubString(row.getKey(), this.replaceTable.get(row.getKey()));
                out.println("<tr><td>" + s + "</td><td>" + s1.toString() + "</td></tr>");
            }
            out.println("</table></td></tr>");
        }
        out.println("</table>");
        out.close();
    }

    @Override
    public String getState() {
        if(this.results == null)
            return "Error creating file.";
        else {
            return "Processing " + this.fieldProcessor.getProgress().getTaxEnt().getName();
        }
    }

    @Override
    public String getDescription() {
        return "Search and replace dry run (no changes are made)";
    }

    @Override
    public User getOwner() {
        return null;
    }

    public String getResults() {
        try {
            return IOUtils.toString(new FileInputStream(this.results), StandardCharsets.UTF_8);
//            return new LineIterator(new FileReader(this.results));
        } catch (IOException e) {
            e.printStackTrace();
            return "<error>";
//            return Collections.<String>emptyIterator();
        }
    }

    @Override   // from FieldVisitor
    public void process(Object bean, String propertyName, SafeHTMLString value) {
        // iterate through replacement table
        for(Map.Entry<String, String> e : this.replaceTable.entrySet()) {
            if(this.resultMap.containsKey(e.getKey())) {
                this.resultMap.get(e.getKey()).addAll(value.replaceSubStringDry(e.getKey(), e.getValue()));
            } else
                this.resultMap.put(e.getKey(), value.replaceSubStringDry(e.getKey(), e.getValue()));
        }
//        tmp.addAll(value.replaceSubStringDry(search, replace));
    }
}
