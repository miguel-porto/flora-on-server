package pt.floraon.entities;

import java.io.IOException;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.NodeTypes;

public class Attribute extends AttributeVertex implements VertexWrapper {
	private FloraOnGraph graph;
	private Boolean dirty=false;

	public Attribute(Attribute at) {
		super(at);
	}

	public Attribute(AttributeVertex at) {
		super(at);
	}
	
	public Attribute(FloraOnGraph graph,Attribute at) {
		super(at);
		this.graph=graph;
	}

	public Attribute(FloraOnGraph graph,AttributeVertex at) {
		super(at);
		this.graph=graph;
	}

	public Attribute(FloraOnGraph graph, String name,String shortName,String description) throws ArangoException {
		super(name,shortName,description);
		this.graph=graph;
		VertexEntity<AttributeVertex> vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.attribute.toString(), new AttributeVertex(this), false);
		super._id=vertexEntity.getDocumentHandle();
	}

	/**
	 * Associates this attribute with a taxon {@link TaxEnt}.
	 * @param parent
	 * @return 0 if already existing relationship, 1 if created new
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setQualityOf(TaxEnt parent) throws IOException, ArangoException {
		if(this._id==null) throw new IOException("Node "+this.name+" not attached to DB");
		HAS_QUALITY a=new HAS_QUALITY(parent._id,this._id); 
		String query=String.format(
			"UPSERT {_from:'%1$s',_to:'%2$s'} INSERT %3$s UPDATE %3$s IN HAS_QUALITY RETURN OLD ? 0 : 1"
			,parent._id
			,this._id
			,a.toJSONString());
		//System.out.println(query);
		return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
	}

	public int setAttributeOfCharacter(Character character) throws IOException, ArangoException {
		if(this._id==null) throw new IOException("Node "+this.name+" not attached to DB");
		ATTRIBUTE_OF a=new ATTRIBUTE_OF(character._id,this._id); 
		String query=String.format(
				"UPSERT {_from:'%1$s',_to:'%2$s'} INSERT %3$s UPDATE %3$s IN ATTRIBUTE_OF RETURN OLD ? 0 : 1"
				,character._id
				,this._id
				,a.toJSONString());
			//System.out.println(query);
			return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
	}
	
	@Override
	public void saveToDB() throws IOException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
