package pt.floraon.entities;

import java.io.IOException;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.driver.FloraOnGraph;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.NodeTypes;

public class Character extends GeneralNodeWrapper {
	public CharacterVertex baseNode;
	
	public Character(Character at) {
		this.baseNode=new CharacterVertex(at);
		super.baseNode=this.baseNode;
	}

	public Character(CharacterVertex at) {
		this.baseNode=at;
		super.baseNode=this.baseNode;
	}
	
	public Character(FloraOnGraph graph,Character at) {
		this.baseNode=new CharacterVertex(at);
		super.baseNode=this.baseNode;
		this.graph=graph;
	}

	public Character(FloraOnGraph graph,CharacterVertex at) {
		this.baseNode=at;
		super.baseNode=this.baseNode;
		this.graph=graph;
	}

	public Character(FloraOnGraph graph, String name,String shortName,String description) throws ArangoException {
		this.baseNode=new CharacterVertex(name,shortName,description);
		super.baseNode=this.baseNode;
		this.graph=graph;
		VertexEntity<CharacterVertex> vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.character.toString(), this.baseNode, false);
		super.baseNode._id=vertexEntity.getDocumentHandle();
	}

	@Override
	public void commit() throws IOException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
