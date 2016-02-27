package pt.floraon.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.floraon.queryparser.Match;
import pt.floraon.results.SimpleTaxonResult;

public abstract class GQuery extends BaseFloraOnDriver implements IQuery {

	public GQuery(FloraOn driver) {
		super(driver);
	}

	@Override
    public List<SimpleTaxonResult> fetchMatchSpecies(Match match,boolean onlyLeafNodes,boolean onlyCurrent) throws DatabaseException {
		Map<String,SimpleTaxonResult> out;
		SimpleTaxonResult tmp2;
		List<SimpleTaxonResult> tmp1=speciesTextQuerySimple(match.query,match.getMatchType(),onlyLeafNodes,onlyCurrent,new String[]{match.getNodeType().toString()},match.getRank());
		// when there is more than one path to the result node, it will come out repeatedly, one time for each path.
		// so we make this unique here, and choose the shortest path.
		out=new HashMap<String,SimpleTaxonResult>();
		// now remove duplicated taxon matches according to priority.
		for(SimpleTaxonResult str : tmp1) {
			if(out.containsKey(str.getTaxonId())) {		// result already exists, let's see if this one has priority over exiting one
				tmp2=out.get(str.getTaxonId());
				if(str.hasPriorityOver(tmp2)) out.put(str.getTaxonId(), str);
			} else
				out.put(str.getTaxonId(), str);
		}
		return new ArrayList<SimpleTaxonResult>(out.values());
    }
}
