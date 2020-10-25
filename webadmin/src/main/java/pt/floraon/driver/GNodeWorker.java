package pt.floraon.driver;

import org.apache.commons.lang.mutable.MutableBoolean;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeWorker;
import pt.floraon.occurrences.Common;
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
    public List<TaxEnt> getTaxEntByName(String q, boolean strict) throws FloraOnException {
		return getTaxEnt(TaxEnt.parse(q), null, strict);
    }

	@Override
    public List<TaxEnt> matchTaxEntToTaxEntList(TaxEnt query, Iterator<TaxEnt> nodes, MutableBoolean askQuestion, boolean strict) {
		// TODO: think better about this match. When no Sensu is given, choose by default the accepted name?
		// FIXME: infrataxa do not work!
		TaxEnt tmp;
		List<TaxEnt> out = new ArrayList<>();
		boolean ask = true;

		// Genus species rank infrataxon Author [annotation] sensu somework
		while (nodes.hasNext()) {
			int levenName;
			boolean tmpask = false;
			tmp = nodes.next();
//			Log.info("    trying: " + tmp.getFullName());
/*			System.out.println("COMPARE");
			System.out.println(query.getName() + " <-> " + tmp.getName());
			System.out.println(query.getRankValue() + " <-> " + tmp.getRankValue());
			System.out.println(query.getAuthor() + " <-> " + tmp.getAuthor());
			System.out.println(query.getAnnotation() + " <-> " + tmp.getAnnotation());
			System.out.println(query.getSensu() + " <-> " + tmp.getSensu());*/

			if (!query.getName().equals(tmp.getName())) {
				if ((levenName = Common.levenshteinDistance(query.getName(), tmp.getName())) >= (strict ? 1 : 3))
					continue;
				else
					tmpask = true;
			} else
				levenName = 0;

			if (query.getRankValue() != null && !query.getRankValue().equals(Constants.TaxonRanks.NORANK.getValue())
					&& !query.getRankValue().equals(tmp.getRankValue())) {
//				nodes.remove();
				continue;
			}

			if (query.getAuthor() != null && !query.getAuthor().equals(tmp.getAuthor()) && levenName < 3) {
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

			if (query.getAnnotation() != null && !query.getAnnotation().equals(tmp.getAnnotation())
					|| (query.getAnnotation() == null && tmp.getAnnotation() != null)) {
			 	continue;
			}

			if ((query.getSensu() != null && !query.getSensu().equals(tmp.getSensu()))) {
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
			if(te.getName().equals(query.getName())) {
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
		} else {
			if (nrExactMatches == 1) {
				out.clear();
				out.add(exactMatch);
			}
		}
		return out;
	}
}
