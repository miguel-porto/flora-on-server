package pt.floraon.taxonomy.entities;

import pt.floraon.driver.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a taxonomic hierarchy: an unidirectional chain of taxa connected with PART_OF relationships
 * Created by miguel on 20-03-2017.
 */
public class TaxonomicPath {
    private List<TaxEnt> path;
    private Map<Constants.TaxonRanks, TaxEnt> pathMap;

    TaxonomicPath(List<TaxEnt> path) {
        this.path = path;
    }

    public TaxEnt getTaxonOfRank(Constants.TaxonRanks rank) {
        if(this.pathMap == null) buildMap();

        return pathMap.get(rank);
    }

    private void buildMap() {
        pathMap = new HashMap<>();
        for(TaxEnt te : path) {
            pathMap.put(te.getRank(), te);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.path.size() - 2; i++) {
            sb.append(this.path.get(i).getName()).append(" : ");
        }
        sb.append(this.path.get(this.path.size() - 2).getName());

        return sb.toString();
    }
}
