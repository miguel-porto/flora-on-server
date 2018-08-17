package pt.floraon.redlistdata;

import java.util.*;

/**
 * Represents a much simplified "version" of {@link pt.floraon.redlistdata.entities.RedListDataEntity} but for all taxa, used for
 * computing aggregated statistics
 */
public class RedListDataEntitySummary {
    private Map<String, Set<Object>> fields = new HashMap<>();
    private List<Set<Object>> plainFields;
    private boolean isReady = false;

    public void addForTaxon(String taxon, Object property) {
        if(property == null) return;
        if(fields.containsKey(taxon))
            fields.get(taxon).add(property);
        else {
            Set<Object> tmp;
            fields.put(taxon, tmp = new HashSet<Object>());
            tmp.add(property);
        }
        isReady = false;
    }

    private void compute() {
        plainFields = new ArrayList<>();
        plainFields.addAll(fields.values());
        fields = new HashMap<>();
        isReady = true;
    }

    public int getCountsForProperty(Object property) {
        int count = 0;
        if(!isReady) compute();

        for(Set<Object> s : plainFields) {
            if(s.contains(property)) count++;
        }
        return count;
    }

    public int getCountsForPropertyIntersection(Object... properties) {
        int count = 0;
        if(!isReady) compute();

        for(Set<Object> s : plainFields) {
            boolean all = true;
            for(Object property : properties) {
                if (!s.contains(property)) {
                    all = false;
                    break;
                }
            }
            if(all) count++;
        }
        return count;
    }
}
