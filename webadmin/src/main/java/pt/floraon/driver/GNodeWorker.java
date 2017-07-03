package pt.floraon.driver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang.mutable.MutableBoolean;
import pt.floraon.occurrences.Common;
import pt.floraon.occurrences.entities.SpeciesList;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * General DB-free routines for working with nodes. Any implementation should extend this class.
 * @author miguel
 *
 */
public abstract class GNodeWorker extends BaseFloraOnDriver implements INodeWorker {
    public GNodeWorker(IFloraOn driver) {
		super(driver);
	}

    @Override
	public TaxEnt createTaxEntFromTaxEnt(TaxEnt te) throws TaxonomyException, FloraOnException {
		return createTaxEntFromName(te.getName(), te.getAuthor(), te.getRank(), te.getSensu(), te.getAnnotation(), te.getCurrent());
	}

    /**
	 * Constructs a new species list from a JSON document as documented in the wiki.
	 * @param sl A {@link JsonObject} as documented <a href="https://github.com/miguel-porto/flora-on-server/wiki/Document-formats-for-uploading-data">here</a>.
	 * @throws FloraOnException
	 */
    @Override
	@Deprecated
	public SpeciesList createSpeciesList(JsonObject sl) throws FloraOnException {
		if(!(sl.has("latitude") && sl.has("longitude") && sl.has("precision") && sl.has("authors") && sl.has("taxa"))) throw new FloraOnException("Species list document must have at least the fields latitude, longitude, precision, authors, taxa.");
		JsonElement tmp;
		// FIXME HERE!!!
		SpeciesList out=new SpeciesList(
			sl.get("latitude").getAsFloat()
			, sl.get("longitude").getAsFloat()
			, (tmp=sl.get("year")) == null ? null : tmp.getAsInt()
			, (tmp=sl.get("month")) == null ? null : tmp.getAsInt()
			, (tmp=sl.get("day")).isJsonNull() ? null : tmp.getAsInt()
			, sl.get("precision").getAsInt()
			, (tmp=sl.get("area")) == null ? null : tmp.getAsInt()
			, (tmp=sl.get("pubNotes")) == null ? null : tmp.getAsString()
			, (tmp=sl.get("complete")) == null ? null : tmp.getAsBoolean()
			, (tmp=sl.get("privNotes")).isJsonNull() ? null : tmp.getAsString()
			, (tmp=sl.get("habitat")) == null ? null : tmp.getAsString()
			);
	
		return createSpeciesList(out);
	}

	@Override
    public List<TaxEnt> getTaxEntByName(String q) throws FloraOnException {
		return getTaxEnt(TaxEnt.parse(q), null);
    }

	@Override
    public List<TaxEnt> matchTaxEntToTaxEntList(TaxEnt q, Iterator<TaxEnt> nodes, MutableBoolean askQuestion) throws FloraOnException {
		// TODO: think better about this match. When no Sensu is given, choose by default the accepted name?
		TaxEnt tmp;
		List<TaxEnt> out = new ArrayList<>();
		boolean ask = true;

		// Genus species rank infrataxon Author [annotation] sensu somework
		while (nodes.hasNext()) {
			int levenName;
			boolean tmpask = false;
			tmp = nodes.next();
//			Log.info("    trying: " + tmp.getFullName());
/*
			System.out.println(q.getRankValue() + " : " + tmp.getRankValue());
			System.out.println(q.getAuthor() + " : " + tmp.getAuthor());
			System.out.println(q.getAnnotation() + " : " + tmp.getAnnotation());
			System.out.println(q.getSensu() + " : " + tmp.getSensu());
*/
			if (!q.getName().equals(tmp.getName())) {
				if ((levenName = Common.levenshteinDistance(q.getName(), tmp.getName())) >= 3)
					continue;
				else
					tmpask = true;
			} else
				levenName = 0;

			if (q.getRankValue() != null && !q.getRankValue().equals(Constants.TaxonRanks.NORANK.getValue())
					&& !q.getRankValue().equals(tmp.getRankValue())) {
//				nodes.remove();
				continue;
			}

			if (q.getAuthor() != null && !q.getAuthor().equals(tmp.getAuthor()) && levenName < 3) {
				tmpask = true;
/*
				if(Common.levenshteinDistance(q.getAuthor(), tmp.getAuthor()) >= 5)
					it.remove();
*/
			}
/*
			if(q.getAuthor() != null && !q.getAuthor().equals(tmp.getAuthor())) {
				if(Common.levenshteinDistance(q.getAuthor(), tmp.getAuthor()) >= 5 && !q.getName().equals(tmp.getName())) {
					it.remove();
					continue;
				} else tmpask = true;
			}
*/

			if (q.getAnnotation() != null && !q.getAnnotation().equals(tmp.getAnnotation())
					|| (q.getAnnotation() == null && tmp.getAnnotation() != null)) {
			 	continue;
			}

			if ((q.getSensu() != null && !q.getSensu().equals(tmp.getSensu()))) {
//					|| (q.getSensu() == null && tmp.getSensu() != null)) {
//				nodes.remove();
				continue;
			}

			ask &= tmpask;
			out.add(tmp);
		}
		int nrExactMatches = 0;
		TaxEnt exactMatch = null;
		for(TaxEnt te : out) {
			if(te.getName().equals(q.getName())) {
				exactMatch = te;
				nrExactMatches++;
			}
		}
/*
		int eq = 0;
		for (TaxEnt te : nodes) {
			if (te.getName().equals(q.getName())) eq++;
		}
*/
		if(askQuestion != null) {
			askQuestion.setValue(out.size() == 0 || (ask && out.size() > 0 && nrExactMatches != 1) || (!ask && out.size() > 1 && nrExactMatches != 1));
			if (nrExactMatches == 1 && askQuestion.getValue() != null && !askQuestion.booleanValue()) {
				out.clear();
				out.add(exactMatch);
			}
		}
		return out;
	}
}
