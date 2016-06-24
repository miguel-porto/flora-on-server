package pt.floraon.arangodriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.NonUniqueResultException;

import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.GTaxEntWrapper;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.ITaxEntWrapper;
import pt.floraon.driver.TaxonomyException;
import static pt.floraon.driver.Constants.*;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.PhenologicalStates;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.entities.EXISTS_IN;
import pt.floraon.entities.OBSERVED_IN;
import pt.floraon.entities.TaxEnt;

/**
 * A node wrapper for TaxEnt-specific operations. A TaxEnt must be provided to work on.
 * @author miguel
 *
 */
public class TaxEntWrapperDriver extends GTaxEntWrapper implements ITaxEntWrapper {
	protected ArangoDriver dbDriver;
	public TaxEntWrapperDriver(FloraOn driver, INodeKey tev) {
		super(driver, tev);
		this.dbDriver=(ArangoDriver) driver.getArangoDriver();
	}

	public Boolean isHybrid() {
		// TODO hybrids!
		return null;
	}
	
	@Override
	public boolean isLeafNode() throws FloraOnException {
		String query="RETURN LENGTH(FOR e IN PART_OF FILTER e._to=='"+node+"' RETURN e)";
		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult()==0;
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getSynonyms() throws FloraOnException {
		String query=String.format(//"FOR u IN UNIQUE(FOR v IN TRAVERSAL(%1$s, %2$s, '%3$s', 'inbound',{paths:false}) FILTER v.vertex._id!='%3$s' RETURN v.vertex) RETURN u"
			"FOR v IN 1..100 ANY '%3$s' %2$s FILTER v._id!='%3$s' RETURN DISTINCT v"
			,NodeTypes.taxent.toString(),RelTypes.SYNONYM.toString(),node
		);
		try {
			return dbDriver.executeAqlQuery(query,null,null,TaxEnt.class).iterator();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public TaxEnt getParentTaxon() throws TaxonomyException, DatabaseException {
		String query=String.format(//"FOR n IN NEIGHBORS(%1$s,%2$s,'%3$s','outbound',{current:true},{includeData:true}) RETURN n"
			"FOR v,e IN 1..1 OUTBOUND '%3$s' %2$s,%4$s FILTER v.current && (HAS(e,'current') && e.current) || !HAS(e,'current') RETURN v"
			,NodeTypes.taxent.toString(),RelTypes.PART_OF.toString(),node.toString(),RelTypes.HYBRID_OF.toString());
		TaxEnt out;
		try {
			out=dbDriver.executeAqlQuery(query,null,null,TaxEnt.class).getUniqueResult();
		} catch (NonUniqueResultException e) {
			throw new TaxonomyException("The taxon "+node.toString()+" has more than one current parent taxon. This must be fixed.");	// TODO: what about hybrids?
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		//if(out==null) throw new TaxonomyException("The taxon "+node.toString()+" has no parent taxon. This must be fixed.");
		return out;
	}

	@Override
	public String[] getEndemismDegree() throws FloraOnException {
		String query=String.format(
			"FILTER DOCUMENT('%1$s').worldDistributionCompleteness=='DISTRIBUTION_COMPLETE' "
			+"FOR v,e IN 1..100 OUTBOUND '%1$s' ANY SYNONYM,EXISTS_IN,PART_OF "
			+"FILTER e.nativeStatus IN [%3$s] RETURN v.name"
			,node,RelTypes.EXISTS_IN.toString(),"'"+implode("','",NativeStatuses)+"'"
		);
		
		List<String> list;
		try {
			list = dbDriver.executeAqlQuery(query,null,null,String.class).asList();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		String[] out = list.toArray(new String[list.size()]);
		return out;
	}

	public List<TaxEnt> getHybridAncestry() {
		// TODO get parent nodes
		return new ArrayList<TaxEnt>();
	}

	@Override
	public int setObservedIn(INodeKey slist,Short doubt,Short validated,PhenologicalStates state,String uuid,Integer weight,String pubnotes,String privnotes,NativeStatus nstate,String dateInserted) throws FloraOnException {
		OBSERVED_IN a=new OBSERVED_IN(doubt,validated,state,uuid,weight,pubnotes,privnotes,nstate,dateInserted,node.toString(),slist.getID().toString());
		String query=String.format(
			"UPSERT {_from:'%1$s',_to:'%2$s'} INSERT %3$s UPDATE %3$s IN OBSERVED_IN RETURN OLD ? 0 : 1"
			,node
			,slist.getID()
			,a.toJSONString());
		//System.out.println(query);
		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}

		/*
		String query=String.format("FOR v IN GRAPH_EDGES ('%1$s',{_from:'%2$s',_to:'%3$s'},{}) COLLECT WITH COUNT INTO l RETURN l",Constants.TAXONOMICGRAPHNAME,this.getID(),slist.getID());
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		if(nrel==0) {
			this.graph.driver.createEdge(AllRelTypes.OBSERVED_IN.toString(), new OBSERVED_IN(doubt,state), this.getID(), slist.getID(), false, false);
			return 1;
		} else return 0;*/
	}

	@Override
	public Iterator<TaxEnt> getChildren() throws FloraOnException {
//		String query=String.format("FOR v IN NEIGHBORS(%1$s, %2$s, '%3$s', 'inbound') LET v1=DOCUMENT(v) SORT v1.name RETURN v1",NodeTypes.taxent.toString(),RelTypes.PART_OF.toString(),node);
		String query=String.format("FOR v1 IN (FOR v,e,p IN 1..2 INBOUND '%2$s' %1$s,%3$s FILTER "
				+ "(LENGTH(p.edges)==1 && PARSE_IDENTIFIER(p.edges[0]).collection=='PART_OF') || "
				+ "(LENGTH(p.edges)==2 && PARSE_IDENTIFIER(p.edges[0]).collection=='PART_OF' && PARSE_IDENTIFIER(p.edges[1]).collection=='HYBRID_OF') "
				+ "RETURN DISTINCT v) SORT v1.name RETURN v1"
				,RelTypes.PART_OF.toString(),node,RelTypes.HYBRID_OF.toString());
	    try {
			return dbDriver.executeAqlQuery(query, null, null, TaxEnt.class).iterator();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}
	
	@Override
	public int setNativeStatus(INodeKey territory, NativeStatus status, OccurrenceStatus occurrenceStatus, AbundanceLevel abundanceLevel, Boolean uncertainOccurrenceStatus) throws FloraOnException {
		String query;
		if(status == null) {	// remove the EXISTS_IN link, if it exists
			query=String.format(
				"FOR e IN EXISTS_IN FILTER e._from=='%1$s' && e._to=='%2$s' REMOVE e IN EXISTS_IN RETURN OLD ? 0 : 1"
				,node.toString()
				,territory.toString());
		} else {				// create or update the EXISTS_IN link
			EXISTS_IN eIn=new EXISTS_IN(status, occurrenceStatus, abundanceLevel, uncertainOccurrenceStatus, node.toString(), territory.toString());
			query=String.format(
				"UPSERT {_from:'%1$s',_to:'%2$s'} INSERT %3$s UPDATE %3$s IN EXISTS_IN RETURN OLD ? 0 : 1"
				,node.toString()
				,territory.toString()
				,eIn.toJSONString());
		}

		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getIncludedTaxa() throws FloraOnException {
		String query=String.format("FOR v IN 1..1 INBOUND '%2$s' %1$s FILTER !v.current RETURN v"
			,RelTypes.PART_OF.toString(),node
		);
		try {
			return dbDriver.executeAqlQuery(query,null,null,TaxEnt.class).iterator();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

}