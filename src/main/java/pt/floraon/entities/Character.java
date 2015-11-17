package pt.floraon.entities;

import java.io.IOException;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.NodeTypes;

public class Character extends CharacterVertex implements VertexWrapper {
	private FloraOnGraph graph;
	private Boolean dirty=false;

	public Character(Character at) {
		super(at);
	}

	public Character(CharacterVertex at) {
		super(at);
	}
	
	public Character(FloraOnGraph graph,Character at) {
		super(at);
		this.graph=graph;
	}

	public Character(FloraOnGraph graph,CharacterVertex at) {
		super(at);
		this.graph=graph;
	}

	public Character(FloraOnGraph graph, String name,String shortName,String description) throws ArangoException {
		super(name,shortName,description);
		this.graph=graph;
		VertexEntity<CharacterVertex> vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.character.toString(), new CharacterVertex(this), false);
		super._id=vertexEntity.getDocumentHandle();
	}

	@Override
	public void saveToDB() throws IOException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
