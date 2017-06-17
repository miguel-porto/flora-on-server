package pt.floraon.driver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jline.internal.Log;
import org.apache.commons.lang.mutable.MutableBoolean;
import pt.floraon.occurrences.Common;
import pt.floraon.occurrences.entities.Author;
import pt.floraon.occurrences.entities.SpeciesList;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.driver.results.Occurrence;

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
	/**
	 * Writes this occurrence to DB. This may involve creating a new SpeciesList and/or creating a new OBSERVED_IN relation.
	 * Does not add anything if an very similar occurrence already exists.
	 * @return
	 * @throws FloraOnException
	 */
    @Override
	@Deprecated
	public void createOccurrence(Occurrence occ) throws FloraOnException {
		// FIXME!
		ISpeciesListWrapper slwd;
		// search for an existing species list in the same coordinates, same author and same date
		SpeciesList tmp=occ.getSpeciesList();
		SpeciesList speciesList = driver.getQueryDriver().findExistingSpeciesList(
			occ.getIdAuts()[0]
			,tmp.getLocation()[0]
			,tmp.getLocation()[1]
			,tmp.getYear()
			,tmp.getMonth()
			,tmp.getDay(),3);
		
		Author autnode;
		if(speciesList == null) {	// add new specieslist
			autnode = getAuthorById(occ.getIdAuts()[0]);		//find 1st author (main)
			if(autnode == null)
				throw new FloraOnException("Cannot find main author with idAut="+occ.getIdAuts()[0]);
			else {	// first author exists and taxon exists, create node
				//sl = new SpeciesList(graph,occ.location[0],occ.location[1],occ.year,occ.month,occ.day,occ.precision,null,null,false,null,null);
				slwd = driver.wrapSpeciesList(driver.asNodeKey(createSpeciesList(occ.getSpeciesList()).getID()));
				slwd.setObservedBy(autnode, true);
			}
			
			// add supplementary observers
			for(int i=1;i<occ.getIdAuts().length;i++) {
				autnode = getAuthorById(occ.getIdAuts()[i]);		//find 1st author (main)
				if(autnode == null)			// SKIP line, main observer is compulsory
					throw new FloraOnException("Cannot find author with idAut="+occ.getIdAuts()[i]);
				else
					slwd.setObservedBy(autnode, false);
			}
		} else slwd = driver.wrapSpeciesList(driver.asNodeKey(speciesList.getID()));
		
		TaxEnt taxnode = getTaxEntByOldId(occ.getIdEnt());	// find taxon with ident, we assume there's only one!

		if(taxnode == null)	// taxon not found! SKIP line
			throw new FloraOnException("Taxon with oldID "+occ.getIdEnt()+" not found.");
		occ.setName(taxnode.getName());
		
		ITaxEntWrapper tewd=driver.wrapTaxEnt(driver.asNodeKey(taxnode.getID()));
		tewd.setObservedIn(driver.asNodeKey(speciesList.getID()), occ.getObservation().getConfidence(), occ.getObservation().getValidated(), occ.getObservation().getPhenoState(), occ.getObservation().getUUID(), occ.getObservation().getWeight(), occ.getObservation().getPublicComment(), occ.getObservation().getPrivateComment(), occ.getObservation().getNativeStatus(), occ.getObservation().getDateInserted());
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
				if ((levenName = Common.levenshteinDistance(q.getName(), tmp.getName())) >= 3) {
//					nodes.remove();
					continue;
				} else tmpask = true;
			} else levenName = 0;

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
//				nodes.remove();
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
/*
		int eq = 0;
		for (TaxEnt te : nodes) {
			if (te.getName().equals(q.getName())) eq++;
		}
*/
		if(askQuestion != null)
			askQuestion.setValue(out.size() == 0 || (ask && out.size() > 0) || (!ask && out.size() > 1));
		return out;
	}
}
