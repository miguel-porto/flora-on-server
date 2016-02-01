package pt.floraon.entities;


import java.util.Objects;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.driver.ArangoKey;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.TerritoryTypes;

public class Territory extends GeneralNodeWrapper {
	public TerritoryVertex baseNode;
	
	public Territory(TerritoryVertex tv) {
		this.baseNode=tv;
		super.baseNode=this.baseNode;
	}
	
	public Territory(FloraOnDriver graph,TerritoryVertex tv) throws FloraOnException {
		if(tv==null) throw new FloraOnException("Null territory given");
		this.baseNode=tv;
		super.baseNode=this.baseNode;
		this.graph=graph;
	}

	private Territory(String name,String shortName, TerritoryTypes type, String theme) throws FloraOnException {
		this.baseNode=new TerritoryVertex(name, shortName, type, theme, true);
		super.baseNode=this.baseNode;
	}

	public static Territory newFromName(FloraOnDriver driver, String name, String shortName, TerritoryTypes type, String theme, TerritoryVertex parent) throws ArangoException, FloraOnException {
		Territory out=new Territory(name, shortName, type, theme);
		out.graph=driver;
		VertexEntity<TerritoryVertex> tmp=driver.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.territory.toString(), out.baseNode, false);
		out.baseNode._id=tmp.getDocumentHandle();
		out.baseNode._key=tmp.getDocumentKey();
		
		if(parent!=null) {
			out.setPART_OF(parent);
			//driver.driver.createEdge(RelTypes.PART_OF.toString(), new PART_OF(), out.baseNode._id, parent._id, false, false);
		}
		return out;
	}

	public static Territory newFromName(FloraOnDriver driver,String name,String shortName, TerritoryTypes type, String theme, ArangoKey parent) throws ArangoException, FloraOnException {
		// NOTE: parent is not checked for the node type. It must be a territory, but is not checked. This is done in the validation.
		Territory out=new Territory(name, shortName, type, theme);
		out.graph=driver;
		VertexEntity<TerritoryVertex> tmp=driver.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.territory.toString(), out.baseNode, false);
		out.baseNode._id=tmp.getDocumentHandle();
		out.baseNode._key=tmp.getDocumentKey();
		
		if(parent!=null) out.setPART_OF(parent);
		return out;
	}

	public int setTaxEntNativeStatus(ArangoKey taxent, NativeStatus status) throws FloraOnException, ArangoException {
		if(baseNode._id==null) throw new FloraOnException("Node "+baseNode.name+" not attached to DB");
		String query;
		if(status == null) {	// remove the EXISTS_IN link, if it exists
			query=String.format(
				"FOR e IN EXISTS_IN FILTER e._from=='%1$s' && e._to=='%2$s' REMOVE e IN EXISTS_IN RETURN OLD ? 0 : 1"
				,taxent.toString()
				,baseNode._id);
		} else {				// create or update the EXISTS_IN link
			EXISTS_IN a=new EXISTS_IN(status, taxent.toString(), baseNode._id);
			query=String.format(
				"UPSERT {_from:'%1$s',_to:'%2$s'} INSERT %3$s UPDATE %3$s IN EXISTS_IN RETURN OLD ? 0 : 1"
				,taxent.toString()
				,baseNode._id
				,a.toJSONString());
		}
		//System.out.println(query);
		return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
	}
	
	public void setName(String name) throws TaxonomyException {
		if(Objects.equals(name, baseNode.name)) return;
		if(name==null || name.trim().length()==0) throw new TaxonomyException("Territory must have a name");
		baseNode.name = name;
		this.dirty=true;
	}

	public void setShowInChecklist(boolean show) throws TaxonomyException {
		if(Objects.equals(show, baseNode.showInChecklist)) return;
		baseNode.showInChecklist = show;
		this.dirty=true;
	}

	public void setShortName(String shortName) throws TaxonomyException {
		if(Objects.equals(shortName, baseNode.shortName)) return;
		if(shortName==null || shortName.trim().length()==0) throw new TaxonomyException("Territory must have a short name");
		baseNode.shortName = shortName;
		this.dirty=true;
	}

	public void setTheme(String theme) throws TaxonomyException {
		if(Objects.equals(theme, baseNode.theme)) return;
		baseNode.theme = theme;
		this.dirty=true;
	}

	public void setType(TerritoryTypes type) throws TaxonomyException {
		if(type==null) throw new TaxonomyException("Territory must have a type");
		if(Objects.equals(type.toString(), baseNode.territoryType)) return;
		baseNode.territoryType = type.toString();
		this.dirty=true;
	}

	@Override
	public void commit() throws FloraOnException, ArangoException {
		if(!this.dirty) return;
		if(baseNode._id==null) throw new FloraOnException("Node "+baseNode.name+" not attached to DB");
		this.graph.driver.graphUpdateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.territory.toString(), baseNode._key, new TerritoryVertex(this), false);
		this.dirty=false;
	}
}
