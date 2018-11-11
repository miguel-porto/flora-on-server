package pt.floraon.arangodriver;

import java.util.*;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.Constants;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.GQuery;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.IQuery;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.StringMatchTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geocoding.entities.MatchedToponym;
import pt.floraon.geocoding.entities.Toponym;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Polygon;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.InventoryIterator;
import pt.floraon.occurrences.OccurrenceIterator;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.queryparser.Match;
import pt.floraon.driver.results.SimpleTaxonResult;
import pt.floraon.taxonomy.entities.TaxEnt;

public class QueryDriver extends GQuery implements IQuery {
	protected ArangoDB dbDriver;
	protected ArangoDatabase database;

	public QueryDriver(IFloraOn driver) {
		super(driver);
		dbDriver = (ArangoDB) driver.getDatabaseDriver();
		database = (ArangoDatabase) driver.getDatabase();
	}

    @Override
	@Deprecated
	public Iterator<Inventory> findInventoriesWithin(Float latitude, Float longitude, Float distance) throws FloraOnException {
    	String query=String.format("RETURN WITHIN(%4$s,%1$f,%2$f,%3$f,'dist')",latitude,longitude,distance,NodeTypes.inventory.toString());
		try {
			return database.query(query, null, null, Inventory.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	@Deprecated
	public Inventory findExistingInventory(int idAuthor, float latitude, float longitude, Integer year, Integer month, Integer day, float radius) throws FloraOnException {
		return null;
/*
		StringBuilder sb=new StringBuilder();
		sb.append("FOR sl IN WITHIN(%1$s,%2$f,%3$f,%4$f) FILTER sl.year==")
			.append(year).append(" && sl.month==")
			.append(month).append(" && sl.day==")
			.append(day)
			.append(" LET nei=GRAPH_NEIGHBORS('%6$s',sl,{direction:'outbound',neighborExamples:{idAut:%5$d},edgeExamples:{main:true},edgeCollectionRestriction:'OBSERVED_BY',includeData:true}) FILTER LENGTH(nei)>0 RETURN sl");

		String query=String.format(sb.toString(), NodeTypes.specieslist.toString(),latitude,longitude,radius,idAuthor,Constants.TAXONOMICGRAPHNAME);

		ArangoCursor<Inventory> vertexCursor;
		try {
			vertexCursor = database.query(query, null, null, Inventory.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}

		if(!vertexCursor.hasNext())
			return null;

		Inventory out = vertexCursor.next();
		if(vertexCursor.hasNext())
			System.out.println("\nWarning: more than one species list found on "+latitude+" "+longitude+", selecting one randomly.");

		return out;
*/
	}
	
	@Override
    public List<Match> queryMatcher(String q,StringMatchTypes matchtype,String[] collections) throws DatabaseException {
    	String query;
    	q=q.toLowerCase().trim();
    	String filter="";
    	switch(matchtype) {
    	case EXACT:
    		filter="LOWER(v.name)=='%2$s'";
			break;
		case PARTIAL:
			filter="LIKE(v.name,'%%%2$s%%',true)";
			break;
		case PREFIX:
			filter="LIKE(v.name,'%2$s%%',true)";
			break;
		default:
			break;
    	}
    	
    	if(collections==null) collections=new String[] {"taxent"};

		// this is actually a workaround so we don't use GRAPH_VERTICES when there are more than 1 collection in the filters, it's faster to do separately
    	List<Match> res=new ArrayList<Match>();
    	for(String collection : collections) {
	    	query=String.format("FOR v IN %3$s FILTER "+filter+" "
    			+ "LET co=PARSE_IDENTIFIER(v._id).collection "
    			+ "LET typematch=LIKE(v.name,'%2$s',true) ? 0 : (LIKE(v.name,'%2$s%%',true) ? 1 : 2) "	// NOTE: these numbers must correspond to the enum order StringMatchTypes
    			+ "COLLECT c=co,r=v.rank,tm=typematch INTO gr SORT tm,r,LENGTH(gr) "
    			+ "RETURN {rank:r,nodeType:c,matchType:tm,matches:gr[*].v.name,query:'%2$s'}"
    			,Constants.TAXONOMICGRAPHNAME,q,collection);
	    	try {
				res.addAll(database.query(query, null, null, Match.class).asListRemaining());
			} catch (ArangoDBException e) {
				throw new DatabaseException(e.getMessage());
			}
    	}
    	return res;
    }

	@Override
	public List<SimpleTaxonResult> speciesTextQuerySimple(ArangoKey node,boolean onlyLeafNodes,boolean onlyCurrent) throws DatabaseException {
		return speciesTextQueryExecutor("DOCUMENT('"+node.toString()+"')", onlyLeafNodes, onlyCurrent);
	}
	
	@Override
	public List<SimpleTaxonResult> speciesTextQuerySimple(String q,StringMatchTypes matchtype,boolean onlyLeafNodes,boolean onlyCurrent,String[] collections,TaxonRanks rank) throws DatabaseException {
    	if(collections==null) collections=new String[] {"taxent"};
    	q=q.toLowerCase().trim();
    	String filter="";
    	switch(matchtype) {
    	case EXACT:
    		filter="LOWER(v.name)=='%2$s'";
			break;
		case PARTIAL:
			filter="LIKE(v.name,'%%%2$s%%',true)";
			break;
		case PREFIX:
			filter="LIKE(v.name,'%2$s%%',true)";
			break;
		default:
			break;
    	}

    	if(rank!=null) {
    		if(filter.equals(""))
    			filter="v.rank=="+rank.getValue().toString();
    		else
    			filter+=" && v.rank=="+rank.getValue().toString();
    	}
    	
    	String baseVertices=String.format("(FOR v IN %1$s FILTER "+filter+" RETURN v._id) ", collections[0], q);
    	return speciesTextQueryExecutor(baseVertices, onlyLeafNodes, onlyCurrent);
	}
	
    public List<SimpleTaxonResult> speciesTextQueryExecutor(String baseVertices,boolean onlyLeafNodes,boolean onlyCurrent) throws DatabaseException {
/*
LET base=(FOR v IN taxent FILTER v.name=='Erodium cicutarium' RETURN v._id)

// THIS IS THE ONLYCURRENT QUERY

FOR final IN FLATTEN(FOR v IN base
    LET allr=(FOR last,e,p IN 1..100 INBOUND v PART_OF,ANY SYNONYM,HYBRID_OF,HAS_QUALITY,EXISTS_IN
        FILTER last.isSpeciesOrInf 
        LET nedg=LENGTH(FOR v2 IN 1..1 INBOUND last PART_OF FILTER v2.current==true RETURN v2)
        //LET notcur=LENGTH(FOR ve IN SLICE(p.vertices,0,LENGTH(p.vertices)-1) FILTER ve.current==false LIMIT 1 RETURN ve)
        LET notcur=LENGTH(FOR ve IN p.edges FILTER ve.current==false LIMIT 1 RETURN ve)
        RETURN DISTINCT {
            taxent: MERGE(last, {isLeaf: nedg==0}), match: [DOCUMENT(v)]
            ,reltypes: (FOR e1 IN p.edges RETURN DISTINCT PARSE_IDENTIFIER(e1._id).collection)
            ,partim: false
            ,notcurrentpath: notcur>0
        })
    // add the self match if it is species or inferior
    LET vd=DOCUMENT(v)
    LET allr1=vd.isSpeciesOrInf ? APPEND(allr,[{
        taxent: MERGE(vd, {isLeaf: LENGTH(FOR v2,e1,p1 IN 1..1 INBOUND vd PART_OF LET last1=p1.vertices[LENGTH(p1.vertices)-1] FILTER last1.current==true RETURN last1)==0})
        , match: [vd], reltypes: []
        ,partim: false
        ,notcurrentpath: false //vd.current!=null && !vd.current
    }]) : allr
    // now we have all results, both current and not current
    // LET real=(FOR r IN allr1 FILTER r.taxent.current RETURN r)     // return current as is
    LET partim=(FOR r IN allr1 FILTER !r.taxent.current        // pick the not current and climb up to the first current
        FOR uv,ue,up IN 1..10 OUTBOUND r.taxent PART_OF
            FILTER uv.current && uv.isSpeciesOrInf && uv._id!=v && LENGTH(FOR tmp1 IN up.vertices FILTER tmp1.current RETURN 1)==1
            LET nedg=LENGTH(FOR v2 IN 1..1 INBOUND uv PART_OF FILTER v2.current==true RETURN v2)
            RETURN DISTINCT {
                taxent: MERGE(uv,{isLeaf: nedg==0})
                ,match: [DOCUMENT(v)]
                ,reltypes: r.reltypes
                ,partim: true
                ,notcurrentpath: false
            })
    RETURN UNION_DISTINCT(allr1,partim)
) RETURN final

// THIS IS THE NOT ONLYCURRENT QUERY
/*******************************
FOR final IN FLATTEN(FOR v IN base
    LET res=(FOR last,e,p IN 1..100 INBOUND v PART_OF,HYBRID_OF,HAS_QUALITY,ANY SYNONYM,EXISTS_IN
        FILTER last.isSpeciesOrInf 
        LET notcuredge=LENGTH(FOR e2 IN p.edges FILTER e2.current==false LIMIT 1 RETURN e2)
        FILTER notcuredge==0	// do not traverse not current edges
        LET nedg=LENGTH(FOR v2 IN 1..1 INBOUND last PART_OF RETURN v2)
        //LET notcur=LENGTH(FOR ve IN SLICE(p.vertices,0,LENGTH(p.vertices)-1) FILTER ve.current!=null && !ve.current LIMIT 1 RETURN ve)
        LET notcur=LENGTH(FOR ve IN p.edges FILTER ve.current==false LIMIT 1 RETURN ve)
        RETURN DISTINCT {
            taxent: MERGE(last, {isLeaf: nedg==0}), match: [DOCUMENT(v)]
            ,reltypes: (FOR e1 IN p.edges RETURN DISTINCT PARSE_IDENTIFIER(e1._id).collection)
            ,partim: false
            ,notcurrentpath: notcur>0
        })
    // add the self match if it is species or inferior
    LET vd=DOCUMENT(v)
    LET allr1=vd.isSpeciesOrInf ? APPEND(res,[{
        taxent: MERGE(vd, {isLeaf: LENGTH(FOR v2 IN 1..1 INBOUND vd PART_OF RETURN v2)==0})
        , match: [vd], reltypes: []
        , partim: false
        , notcurrentpath: false // vd.current!=null && !vd.current
        }]) : res
    RETURN allr1
) RETURN final

 */
    	String leaf=onlyLeafNodes ? " FILTER nedg==0" : " ",query;
    	
		if(onlyCurrent) {// TODO for attributes, the traverser should climb taxonomy uphill when current node is not current
			// TODO: if a non-current node is traversed, must signal it.
			query="LET base="+baseVertices+
				"FOR final IN FLATTEN(FOR v IN base " + 
				"    LET allr=(FOR last,e,p IN 1..100 INBOUND v PART_OF,ANY SYNONYM,HYBRID_OF,HAS_QUALITY,EXISTS_IN " + 
				"        FILTER last.isSpeciesOrInf  " + 
				"        LET nedg=LENGTH(FOR v2 IN 1..1 INBOUND last PART_OF FILTER v2.current==true RETURN v2) " + leaf +
				"        LET notcur=LENGTH(FOR ve IN p.edges FILTER ve.current==false LIMIT 1 RETURN ve) " + 
				"        RETURN DISTINCT { " + 
				"            taxent: MERGE(last, {isLeaf: nedg==0}), match: [DOCUMENT(v)] " +
				"            ,reltypes: (FOR e1 IN p.edges RETURN DISTINCT PARSE_IDENTIFIER(e1._id).collection) " + 
				"            ,partim: false " + 
				"            ,notcurrentpath: notcur>0 " + 
				"        }) " + 
				"    LET vd=DOCUMENT(v) " + 
				"    LET allr1=vd.isSpeciesOrInf ? APPEND(allr,[{ " + 
				"        taxent: MERGE(vd, {isLeaf: LENGTH(FOR v2,e1,p1 IN 1..1 INBOUND vd PART_OF LET last1=p1.vertices[LENGTH(p1.vertices)-1] FILTER last1.current==true RETURN last1)==0}) " +
				"        , match: [vd], reltypes: [] " + 
				"        ,partim: false " + 
				"        ,notcurrentpath: false " + 
				"    }]) : allr " + 
				"    LET real=(FOR r IN allr1 FILTER r.taxent.current RETURN r) " + 
				"    LET partim=(FOR r IN allr1 FILTER !r.taxent.current " + 
				"        FOR uv,ue,up IN 1..10 OUTBOUND r.taxent PART_OF " + 
				"            FILTER uv.current && uv.isSpeciesOrInf && uv._id!=v && LENGTH(FOR tmp1 IN up.vertices FILTER tmp1.current RETURN 1)==1 " + 
				"            LET nedg=LENGTH(FOR v2 IN 1..1 INBOUND uv PART_OF FILTER v2.current==true RETURN v2) " + leaf +
				"            RETURN DISTINCT { " + 
				"                taxent: MERGE(uv,{isLeaf: nedg==0}) " +
				"                ,match: [DOCUMENT(v)] " + 
				"                ,reltypes: r.reltypes " + 
				"                ,partim: true " + 
				"                ,notcurrentpath: false " + 
				"            }) " + 
				"    RETURN UNION_DISTINCT(allr1,partim) " + // real,partim
				") RETURN final "; 
		} else {
			query="LET base="+baseVertices + 
				"FOR final IN FLATTEN(FOR v IN base " + 
				"    LET res=(FOR last,e,p IN 1..100 INBOUND v PART_OF,HYBRID_OF,HAS_QUALITY,ANY SYNONYM,EXISTS_IN " + 
				"        FILTER last.isSpeciesOrInf " + 
				"        LET notcuredge=LENGTH(FOR e2 IN p.edges FILTER e2.current==false LIMIT 1 RETURN e2) " + 
				"        FILTER notcuredge==0 " + 
				"        LET nedg=LENGTH(FOR v2 IN 1..1 INBOUND last PART_OF RETURN v2) " + leaf + 
				"        LET notcur=LENGTH(FOR ve IN p.edges FILTER ve.current==false LIMIT 1 RETURN ve) " + 
				"        RETURN DISTINCT { " + 
				"            taxent: MERGE(last, {isLeaf: nedg==0}), match: [DOCUMENT(v)] " +
				"            ,reltypes: (FOR e1 IN p.edges RETURN DISTINCT PARSE_IDENTIFIER(e1._id).collection) " + 
				"            ,partim: false " + 
				"            ,notcurrentpath: notcur>0 " + 
				"        }) " + 
				"    LET vd=DOCUMENT(v) " + 
				"    LET allr1=vd.isSpeciesOrInf ? APPEND(res,[{ " + 
				"        taxent: MERGE(vd, {isLeaf: LENGTH(FOR v2 IN 1..1 INBOUND vd PART_OF RETURN v2)==0}) " +
				"        , match: [vd], reltypes: [] " + 
				"        , partim: false " + 
				"        , notcurrentpath: false " + 
				"        }]) : res " + 
				"    RETURN allr1 " + 
				") RETURN final ";
		}

		try {
			return database.query(query, null, null, SimpleTaxonResult.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
    }

	@Override
    public Iterator<TaxEnt> findTaxonSuggestions(String query, Integer limit) throws FloraOnException {
    	String limitQ;
		String _query = null;
		if(limit != null) limitQ = "LIMIT " + limit; else limitQ = "";

		if(query.length() == 1)
			_query = AQLQueries.getString("QueryDriver.1", query, limitQ);
    	else if(query.length() > 1) {
			String first = query.substring(0, 1);
			String last = query.substring(1);
			if(query.length() % 2 != 0) {
				_query = AQLQueries.getString("QueryDriver.1a", query, limitQ, first, last);
			} else {
				String firstHalf = query.substring(0, query.length() / 2);
				String lastHalf = query.substring(query.length() / 2);
				_query = AQLQueries.getString("QueryDriver.1b", query, limitQ, first, last, firstHalf, lastHalf);
			}
		}
		if(_query == null) return Collections.emptyIterator();

		try {
			return database.query(_query, null, null, TaxEnt.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
    	// TODO levenshtein, etc.
    }

	@Override
	public Iterator<User> findUserSuggestions(String query, Integer limit) throws FloraOnException {
		String limitQ;
		if(limit != null) limitQ = "LIMIT " + limit; else limitQ = "";
		String _query = AQLQueries.getString("QueryDriver.2", query, limitQ);
		try {
			return database.query(_query, null, null, User.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public List<MatchedToponym> findToponymSuggestions(String query) throws FloraOnException {
		if(query == null || query.trim().length() < 3) return Collections.emptyList();
		Map<String, Object> bindVars = new HashMap<>();
		String tQuery = query.trim().toLowerCase();
		bindVars.put("firstletter", "prefix:" + tQuery.substring(0, 1));
//		bindVars.put("query", "%" + tQuery + "%");
//		bindVars.put("length",tQuery.length());

		// this query needs a FULLTEXT index with minLength=1
		String _queryFull = AQLQueries.getString("QueryDriver.3");
		String first = tQuery.substring(0, 3);
//		String _queryLeven = AQLQueries.getString("QueryDriver.3a", tQuery.substring(0, 2));
		List<MatchedToponym> out = new LinkedList<>();

		try {
			Iterator<Toponym> it = database.query(_queryFull, bindVars, null, Toponym.class);
			while(it.hasNext()) {
				Toponym t = it.next();
				int d = StringUtils.fuzzyWordMatch(t.getLocality().trim().toLowerCase(), tQuery.trim().toLowerCase());

				if(d < 3) {
					MatchedToponym topo = new MatchedToponym(t, d);
					if(!out.contains(topo))
						out.add(topo);
				}
			}


/*
			it = database.query(_queryLeven, null, null, Toponym.class);
			while(it.hasNext()) {
				Toponym t = it.next();
				if(t.getLocality().toLowerCase().equals(tQuery))
					d = 0;
				else if(Math.abs(t.getLocality().length() - tQuery.length()) < 3) {
					d = Common.levenshteinDistance(t.getLocality().toLowerCase(), tQuery);
//					System.out.println(t.getLocality().toLowerCase()+": "+t.getLocality().length()+" "+tQuery.length()+" "+d);
				}
				else d = 1000;
				if(d < 3) {
					MatchedToponym topo = new MatchedToponym(t, d);
					if(!out.contains(topo))
						out.add(topo);
				}
			}
*/

		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		Collections.sort(out);
		return out;
	}

	@Override
	public List<SimpleTaxonResult> findListTaxaWithin(Float latitude,Float longitude,int distance) throws DatabaseException {
		String query=String.format("FOR sl IN WITHIN(%1$s,%2$f,%3$f,%4$d) "
				+ "FOR o IN (FOR n IN NEIGHBORS(specieslist,%5$s,sl,'inbound',{},{includeData:true}) "
				+ "RETURN {match:sl,name:n.name,_id:n._id}) "
				+ "COLLECT k=o._id,n=o.name INTO gr LET ma=gr[*].o.match RETURN {name:n,_id:k,match:ma,count:LENGTH(ma),reltypes:['%5$s']}"
				,NodeTypes.inventory.toString(),latitude,longitude,distance,RelTypes.OBSERVED_IN.toString());
		//System.out.println(query);
    	try {
			return database.query(query, null, null, SimpleTaxonResult.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
    public int getNumberOfNodesInCollection(NodeTypes nodetype) throws FloraOnException {
    	String query="FOR v IN "+nodetype.toString()+" COLLECT WITH COUNT INTO cou RETURN cou";
    	try {
			return database.query(query, null, null, Integer.class).next();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
    }

}
