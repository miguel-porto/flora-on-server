package pt.floraon.entities;

import java.io.IOException;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.RelTypes;

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
	
	public Attribute(FloraOnDriver graph,Attribute at) {
		super.baseNode=new AttributeVertex(at.baseNode);
		this.baseNode=(AttributeVertex)super.baseNode;
		this.graph=graph;
	}

	public Attribute(FloraOnDriver graph,AttributeVertex at) {
		super.baseNode=at;
		this.baseNode=(AttributeVertex)super.baseNode;
		this.graph=graph;
	}

	public Attribute(String name,String shortName,String description) throws ArangoException {
		super.baseNode=new AttributeVertex(name,shortName,description);
		this.baseNode=(AttributeVertex)super.baseNode;
	}

	@Deprecated
	public Attribute(FloraOnDriver graph, String name,String shortName,String description) throws ArangoException {
		super.baseNode=new AttributeVertex(name,shortName,description);
		this.baseNode=(AttributeVertex)super.baseNode;
		this.graph=graph;
		VertexEntity<AttributeVertex> vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.attribute.toString(), this.baseNode, false);
		super.baseNode._id=vertexEntity.getDocumentHandle();
	}

	public static Attribute newFromName(FloraOnDriver driver, String name,String shortName,String description) throws ArangoException {
		Attribute out=new Attribute(name, shortName, description);
		out.graph=driver;
		VertexEntity<AttributeVertex> tmp=driver.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.attribute.toString(), out.baseNode, false);
		out.baseNode._id=tmp.getDocumentHandle();
		out.baseNode._key=tmp.getDocumentKey();
		return out;
	}

	/**
	 * Associates this attribute with a taxon {@link TaxEnt}.
	 * @param parent
	 * @return 0 if already existing relationship, 1 if created new
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setHAS_QUALITY(TaxEnt parent) throws ArangoException, IOException {
		if(baseNode._id==null) throw new IOException("Node "+baseNode._id+" not attached to DB");

		// checks whether there is already a HAS_QUALITY relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,parent.baseNode._id,baseNode._id,RelTypes.HAS_QUALITY.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			this.graph.driver.createEdge(RelTypes.HAS_QUALITY.toString(), new HAS_QUALITY(), parent.baseNode._id, baseNode._id, false, false);
			return 1;
		} else return 0;
	}

	public int setAttributeOfCharacter(Character character) throws IOException, ArangoException {
		if(baseNode._id==null) throw new IOException("Node "+baseNode.name+" not attached to DB");

		// checks whether there is already a HAS_QUALITY relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseNode._id,character.baseNode._id,RelTypes.ATTRIBUTE_OF.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			this.graph.driver.createEdge(RelTypes.ATTRIBUTE_OF.toString(), new ATTRIBUTE_OF(), baseNode._id, character.baseNode._id, false, false);
			return 1;
		} else return 0;
	}
	
	@Override
	public void commit() throws FloraOnException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
