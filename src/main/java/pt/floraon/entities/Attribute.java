package pt.floraon.entities;

import java.io.IOException;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.AllRelTypes;
import pt.floraon.server.Constants.NodeTypes;

public class Attribute extends GeneralNodeWrapper {
	public AttributeVertex baseNode;
	public Attribute(Attribute at) {
		super.baseNode=new AttributeVertex(at.baseNode);
		this.baseNode=(AttributeVertex)super.baseNode;
	}

	public Attribute(AttributeVertex at) {
		super.baseNode=at;
		this.baseNode=(AttributeVertex)super.baseNode;
	}
	
	public Attribute(FloraOnGraph graph,Attribute at) {
		super.baseNode=new AttributeVertex(at.baseNode);
		this.baseNode=(AttributeVertex)super.baseNode;
		this.graph=graph;
	}

	public Attribute(FloraOnGraph graph,AttributeVertex at) {
		super.baseNode=at;
		this.baseNode=(AttributeVertex)super.baseNode;
		this.graph=graph;
	}

	public Attribute(FloraOnGraph graph, String name,String shortName,String description) throws ArangoException {
		super.baseNode=new AttributeVertex(name,shortName,description);
		this.baseNode=(AttributeVertex)super.baseNode;
		this.graph=graph;
		VertexEntity<AttributeVertex> vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.attribute.toString(), this.baseNode, false);
		super.baseNode._id=vertexEntity.getDocumentHandle();
	}


	/**
	 * Associates this attribute with a taxon {@link TaxEnt}.
	 * @param parent
	 * @return 0 if already existing relationship, 1 if created new
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setQUALITY_OF(TaxEnt parent) throws ArangoException, IOException {
		if(baseNode._id==null) throw new IOException("Node "+baseNode._id+" not attached to DB");

		// checks whether there is already a HAS_QUALITY relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseNode._id,parent.baseNode._id,AllRelTypes.HAS_QUALITY.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			this.graph.driver.createEdge(AllRelTypes.HAS_QUALITY.toString(), new HAS_QUALITY(), baseNode._id, parent.baseNode._id, false, false);
			return 1;
		} else return 0;
	}

	public int setAttributeOfCharacter(Character character) throws IOException, ArangoException {
		if(baseNode._id==null) throw new IOException("Node "+baseNode.name+" not attached to DB");

		// checks whether there is already a HAS_QUALITY relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseNode._id,character.baseNode._id,AllRelTypes.ATTRIBUTE_OF.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			this.graph.driver.createEdge(AllRelTypes.ATTRIBUTE_OF.toString(), new ATTRIBUTE_OF(), baseNode._id, character.baseNode._id, false, false);
			return 1;
		} else return 0;
	}
	
	@Override
	public void saveToDB() throws IOException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
