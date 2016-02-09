package pt.floraon.arangodriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.util.GraphVerticesOptions;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.driver.Constants;
import pt.floraon.driver.BaseFloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.IListDriver;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.ChecklistEntry;
import pt.floraon.results.GraphUpdateResult;
import pt.floraon.results.SimpleNameResult;

public class ListDriver extends BaseFloraOnDriver implements IListDriver {
	protected ArangoDriver dbDriver;
	public ListDriver(FloraOn driver) {
		super(driver);
		this.dbDriver=(ArangoDriver) driver.getArangoDriver();
	}

	/**
	 * Gets the complete list of taxa in the DB
	 * @return
	 */
	public List<ChecklistEntry> getCheckList() {
		// TODO the query is very slow!
		List<ChecklistEntry> chklst=new ArrayList<ChecklistEntry>();
        @SuppressWarnings("rawtypes")
		CursorResult<List> vertexCursor;
        @SuppressWarnings("rawtypes")
        Iterator<List> vertexIterator;
    	GraphVerticesOptions gvo=new GraphVerticesOptions();
    	List<String> vcr=new ArrayList<String>();
    	vcr.add("taxent");
    	gvo.setVertexCollectionRestriction(vcr);
    	String query=String.format(
			"FOR v IN GRAPH_TRAVERSAL('%1$s',"	//		LENGTH(EDGES(%2$s,v._id,'inbound'))
			+ "FOR v IN taxent FILTER v.isSpeciesOrInf==true && v.current==true && LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e)==0 RETURN v"	// leaf nodes
			+ ",'outbound',{paths:false,filterVertices:[%3$s],vertexFilterMethod:['exclude']}) COLLECT a=v[*].vertex RETURN a"
   			, Constants.TAXONOMICGRAPHNAME,RelTypes.PART_OF.toString(),Constants.CHECKLISTFIELDS);

    	try {
    		// traverse all leaf nodes outwards
    		vertexCursor=dbDriver.executeAqlQuery(query, null, null, List.class);
			vertexIterator = vertexCursor.iterator();
			
			ChecklistEntry chk;
			while (vertexIterator.hasNext()) {
				@SuppressWarnings("unchecked")
				List<LinkedTreeMap<String,Object>> entry1 = vertexIterator.next();
				chk=new ChecklistEntry();
				for(LinkedTreeMap<String,Object> tev:entry1) {
					TaxEnt te=new TaxEnt(tev);
					if(te.isSpeciesOrInferior()) {
						if(chk.canonicalName==null) {
							chk.taxon=te.getFullName();
							chk.canonicalName=te.getName();
						}
					}
					switch(te.getRank()) {
					case GENUS:
						chk.genus=te.getName();
						break;
					case FAMILY:
						chk.family=te.getName();
						break;
					case ORDER:
						chk.order=te.getName();
						break;
					default:
						break;
					}
				}
				chklst.add(chk);
			}
		} catch (ArangoException | FloraOnException e) {
			e.printStackTrace();
		}
    	return chklst;
	}
	
	@Override
    public GraphUpdateResult getAllTerritoriesGraph(TerritoryTypes territoryType) throws FloraOnException {
    	String query;
    	if(territoryType!=null)
    		query=String.format("FOR v IN %1$s FILTER v.territoryType=='%2$s' RETURN v._id",NodeTypes.territory.toString(), territoryType.toString());
    	else
    		query=String.format("FOR v IN %1$s SORT v.name RETURN v._id",NodeTypes.territory.toString());
    	String[] ids=new String[0];
    	try {
			ids=dbDriver.executeAqlQuery(query, null, null, String.class).asList().toArray(ids);
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    	return driver.getNodeWorkerDriver().getRelationshipsBetween(ids, new Facets[] {Facets.TAXONOMY});
    }

	@Override
    public Iterator<Territory> getChecklistTerritories() throws FloraOnException {
    	String query;
		query=String.format("FOR v IN %1$s FILTER v.showInChecklist==true SORT v.name RETURN v",NodeTypes.territory.toString());
    	try {
			return dbDriver.executeAqlQuery(query, null, null, Territory.class).iterator();
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    }

	@Override
    public Iterator<Territory> getAllTerritories(TerritoryTypes territoryType) throws FloraOnException {
    	String query;
    	if(territoryType!=null)
    		query=String.format("FOR v IN %1$s FILTER v.territoryType=='%2$s' SORT v.name RETURN v",NodeTypes.territory.toString(), territoryType.toString());
    	else
    		query=String.format("FOR v IN %1$s SORT v.name RETURN v",NodeTypes.territory.toString());
    	try {
			return dbDriver.executeAqlQuery(query, null, null, Territory.class).iterator();
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    }


	@Override
	public <T extends SimpleNameResult> Iterator<T> getAllSpeciesOrInferior(boolean onlyLeafNodes, Class<T> T, String territory, Integer offset, Integer count) throws FloraOnException {
		String query;
		boolean withLimit=false;
		if(!(offset==null && count==null)) {
			if(offset==null) offset=0;
			if(count==null) count=50;
			withLimit=true;
		}
/* This is the general query.
LET terr=SLICE(TRAVERSAL(taxent, EXISTS_IN, 'taxent/3014731409815', 'outbound', {maxDepth:1,paths:true}),1)
LET upstr=(FOR t IN terr
LET tt=TRAVERSAL(territory, PART_OF, t.vertex, 'outbound')
LET bs=t.path.edges[0].nativeStatus
RETURN {
    baseStatus: ZIP([t.vertex.shortName], [bs])
    ,upstreamStatus: ZIP(
        SLICE(tt[*].vertex.shortName,1)
        , (FOR i IN 2..LENGTH(tt) RETURN bs == 'ENDEMIC' ? 'ENDEMIC' : 'EXISTING')
    )
})
LET bs=LENGTH(upstr) == 1 ? upstr[0].baseStatus : APPLY("MERGE",upstr[*].baseStatus)
LET an=UNIQUE(FLATTEN(FOR up IN upstr RETURN ATTRIBUTES(up.upstreamStatus)))
LET anc=MINUS(an, ATTRIBUTES(bs))
LET inferr=(FOR ut IN anc
LET tmp=REMOVE_VALUE(UNIQUE(FOR up IN upstr RETURN TRANSLATE(ut,up.upstreamStatus,null)),null)
RETURN ZIP([ut], [LENGTH(tmp) == 1 ? tmp[0] : POSITION(tmp,'ENDEMIC',false) ? 'ENDEMIC' : 'EXISTING']) )
RETURN MERGE(bs, LENGTH(inferr) == 1 ? inferr[0] : APPLY("MERGE",inferr) )
*/
		if(territory==null) {
			query=String.format("FOR v IN %2$s "
				+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) "
				+ "FILTER v.isSpeciesOrInf==true %1$s SORT v.name %3$s "
				+ "RETURN MERGE(KEEP(v,'_id','name','author','current'), {leaf: npar==0, territories: "
				+ "(LET terr=SLICE(TRAVERSAL(taxent, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}),1) "
				+ "LET upstr=(FOR t IN terr LET tt=TRAVERSAL(territory, PART_OF, t.vertex, 'outbound') LET bs=t.path.edges[0].nativeStatus "
				+ "RETURN {baseStatus: ZIP([t.vertex.shortName], [bs]), upstreamStatus: ZIP(SLICE(tt[*].vertex.shortName,1), (FOR i IN 2..LENGTH(tt) RETURN bs == 'ENDEMIC' ? 'ENDEMIC' : 'EXISTING')) }) "
				+ "LET bs=LENGTH(upstr) == 1 ? upstr[0].baseStatus : APPLY('MERGE',upstr[*].baseStatus) LET an=UNIQUE(FLATTEN(FOR up IN upstr RETURN ATTRIBUTES(up.upstreamStatus))) LET anc=LENGTH(an)>0 ? MINUS(an, ATTRIBUTES(bs)) : [] "
				+ "LET inferr=(FOR ut IN anc LET tmp=REMOVE_VALUE(UNIQUE(FOR up IN upstr RETURN TRANSLATE(ut,up.upstreamStatus,null)),null) RETURN ZIP([ut], [LENGTH(tmp) == 1 ? tmp[0] : POSITION(tmp,'ENDEMIC',false) ? 'ENDEMIC' : 'EXISTING']) )"
				+ "RETURN MERGE(bs, LENGTH(inferr) == 1 ? inferr[0] : APPLY('MERGE',inferr) ) "
				+ ")[0]})"
				, onlyLeafNodes ? "&& npar==0" : "", NodeTypes.taxent.toString(), withLimit ? "LIMIT "+offset+","+count : "");
/*				query=String.format("FOR v IN %2$s "
				+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) FILTER v.isSpeciesOrInf==true "
				+ "%1$s SORT v.name %3$s LET terr=TRAVERSAL(%2$s, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}) RETURN {_id:v._id,name:v.name,author:v.author,leaf:npar==0, current:v.current"
				+ ", territories:(LET d=SLICE(terr,1) RETURN ZIP(d[*].vertex.shortName, d[*].path.edges[0].nativeStatus))[0]}"	//DOCUMENT(terr)[*].shortName
				, onlyLeafNodes ? "&& npar==0" : "", NodeTypes.taxent.toString(), withLimit ? "LIMIT "+offset+","+count : "");*/
		} else {
			if(onlyLeafNodes) System.out.println("Warning: possibly omitting taxa from the checklist.");
//FIXME must traverse territories downwards in thr 1st line! Wait for ArangoDB 2.8
			query=String.format(
				"FOR te IN territory FILTER te.shortName=='%3$s' FOR v IN (FOR v1 IN NEIGHBORS(territory, EXISTS_IN, te, 'inbound') RETURN DOCUMENT(v1)) "
				+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) "
				+ "%1$s SORT v.name %4$s "
				+ "RETURN MERGE(KEEP(v,'_id','name','author','current'), {leaf: npar==0, territories: "
				+ "(LET terr=SLICE(TRAVERSAL(taxent, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}),1) "
				+ "LET upstr=(FOR t IN terr LET tt=TRAVERSAL(territory, PART_OF, t.vertex, 'outbound') LET bs=t.path.edges[0].nativeStatus "
				+ "RETURN {baseStatus: ZIP([t.vertex.shortName], [bs]), upstreamStatus: ZIP(SLICE(tt[*].vertex.shortName,1), (FOR i IN 2..LENGTH(tt) RETURN bs == 'ENDEMIC' ? 'ENDEMIC' : 'EXISTING')) }) "
				+ "LET bs=LENGTH(upstr) == 1 ? upstr[0].baseStatus : APPLY('MERGE',upstr[*].baseStatus) LET an=UNIQUE(FLATTEN(FOR up IN upstr RETURN ATTRIBUTES(up.upstreamStatus))) LET anc=LENGTH(an)>0 ? MINUS(an, ATTRIBUTES(bs)) : [] "
				+ "LET inferr=(FOR ut IN anc LET tmp=REMOVE_VALUE(UNIQUE(FOR up IN upstr RETURN TRANSLATE(ut,up.upstreamStatus,null)),null) RETURN ZIP([ut], [LENGTH(tmp) == 1 ? tmp[0] : POSITION(tmp,'ENDEMIC',false) ? 'ENDEMIC' : 'EXISTING']) )"
				+ "RETURN MERGE(bs, LENGTH(inferr) == 1 ? inferr[0] : APPLY('MERGE',inferr) ) "
				+ ")[0]})"
				, onlyLeafNodes ? " FILTER npar==0" : "", NodeTypes.taxent.toString(), territory, withLimit ? "LIMIT "+offset+","+count : "");
/*					"FOR t IN territory FILTER t.shortName=='%3$s' FOR v IN FLATTEN(FOR v1 IN GRAPH_TRAVERSAL('taxgraph', t, 'inbound', {filterVertices: [{isSpeciesOrInf: true}], vertexFilterMethod:'exclude'}) RETURN v1[*].vertex) "
				+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) "
				+ "%1$s LET terr=TRAVERSAL(%2$s, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}) SORT v.name RETURN {_id:v._id,name:v.name,author:v.author,leaf:npar==0, current:v.current"
				+ ", territories:(LET d=SLICE(terr,1) RETURN ZIP(d[*].vertex.shortName, d[*].path.edges[0].nativeStatus))[0]}"	//DOCUMENT(terr)[*].shortName
				, onlyLeafNodes ? " FILTER npar==0" : "", NodeTypes.taxent.toString(), territory);*/
		}
		//System.out.println(query);
    	CursorResult<T> vertexCursor;
		try {
			vertexCursor = dbDriver.executeAqlQuery(query, null, null, T);
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    	return vertexCursor.iterator();
	}

	@Override
	public Iterator<TaxEnt> getAllOfRank(TaxonRanks rank) throws FloraOnException {
		String query=String.format("FOR v IN %1$s FILTER v.rank==%2$d SORT v.name RETURN v"
			,NodeTypes.taxent.toString(),rank.getValue());
    	CursorResult<TaxEnt> vertexCursor;
		try {
			vertexCursor = dbDriver.executeAqlQuery(query, null, null, TaxEnt.class);
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}
    	return vertexCursor.iterator();			
	}
	
	@Override
	public GraphUpdateResult getAllCharacters() {
			String query=String.format("RETURN {nodes:(FOR v IN %1$s "
				+ "RETURN MERGE(v,{type:PARSE_IDENTIFIER(v._id).collection}))"
				+ ",links:[]}"
				,NodeTypes.character.toString()
			);
			String res;
			try {
				res = dbDriver.executeAqlQueryJSON(query, null, null);
			} catch (ArangoException e) {
				System.err.println(e.getErrorMessage());
				return GraphUpdateResult.emptyResult();
			}
			// NOTE: server responses are always an array, but here we always have one element, so we remove the []
			return (res==null || res.equals("[]")) ? GraphUpdateResult.emptyResult() : new GraphUpdateResult(res.substring(1, res.length()-1));
		}
}
