package pt.floraon.driver;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.arangodb.ArangoCursor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import pt.floraon.driver.results.TaxEntAndNativeStatusResult;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.redlistdata.entities.AtomicTaxonPrivilege;
import pt.floraon.redlistdata.entities.RedListDataEntity;

/**
 * Base class for all drivers. Provides database-independent functionality to process things.
 * @author miguel
 *
 */
public class BaseFloraOnDriver {
	protected IFloraOn driver;

	public BaseFloraOnDriver(IFloraOn driver) {
		this.driver=driver;
	}
	
	/**
	 * Processes a list of {@link TaxEntAndNativeStatusResult} from a DB query to extract taxon information in JSON format
	 * @param TENSResult
	 * @return
	 * @throws FloraOnException
	 */
	public JsonArray getTaxonInfoAsJson(List<TaxEntAndNativeStatusResult> TENSResult) throws FloraOnException {
		Iterator<TaxEntAndNativeStatusResult> it = TENSResult.iterator();
		TaxEntAndNativeStatusResult tmp;
		JsonObject tax;
		JsonArray out = new JsonArray();
		Gson gson = new Gson();
		Map<String,InferredStatus> tStatus;
		Set<String> terr = new HashSet<String>();
		terr.addAll(Constants.getIDsList(this.driver.getListDriver().getChecklistTerritories()));
		
		while(it.hasNext()) {
			tmp = it.next();
			tax = new JsonObject();
			if(tmp.getTaxent() != null) {
				tStatus = tmp.inferNativeStatus();
				tax.add("taxon", gson.toJsonTree(tmp.getTaxent()));
				tax.add("endemismDegree", gson.toJsonTree(tmp.inferEndemismDegree()));
				tmp.inferSingleTerritoryEndemismDegree();
				tax.add("restrictedTo", gson.toJsonTree(tmp.inferRestrictedTo(terr)));
				
				JsonObject tst = new JsonObject();
				if(tmp.getTerritoryStatus() != null) {
					for(Entry<String, InferredStatus> st : tStatus.entrySet()) {
						tst.add(st.getKey(), gson.toJsonTree(st.getValue()));
					}
				}
				tax.add("territories", tst);
			}
			out.add(tax);
		}
		return out;
	}

	public static List<String> getPropertyList(Properties properties, String name) {
		List<String> result = new ArrayList<String>();
		for (Map.Entry<Object, Object> entry : properties.entrySet())
		{
			if (((String)entry.getKey()).matches("^" + Pattern.quote(name) + "\\.\\d+$")) {
				result.add((String) entry.getValue());
			}
		}
		return result;
	}

	/**
	 * Takes a red list data object and fills in the responsible authors.
	 * @param atomicPrivilegesIt
	 * @param rldeIt
	 * @return
	 */
	public List<RedListDataEntity> assignResponsibleAuthors(Iterator<AtomicTaxonPrivilege> atomicPrivilegesIt, Iterator<RedListDataEntity> rldeIt) {
		AtomicTaxonPrivilege atp;
		Multimap<String, AtomicTaxonPrivilege> atomicPrivileges = ArrayListMultimap.create();
		while(atomicPrivilegesIt.hasNext()) {
			atp = atomicPrivilegesIt.next();
			atomicPrivileges.put(atp.getTaxEntId(), atp);
		}

		RedListDataEntity rlde;
		List<RedListDataEntity> out = new ArrayList<>();
		while(rldeIt.hasNext()) {
			rlde = rldeIt.next();
			for(AtomicTaxonPrivilege atp1 : atomicPrivileges.get(rlde.getTaxEntID())) {
				if(atp1.isResponsibleForTexts()) rlde.addResponsibleForTexts(atp1.getUserId());
				if(atp1.isResponsibleForAssessment()) rlde.addResponsibleForAssessment(atp1.getUserId());
				if(atp1.isResponsibleForRevision()) rlde.addResponsibleForRevision(atp1.getUserId());
			}
			out.add(rlde);
		}
		return out;
	}

}
