package pt.floraon.entities;

import java.io.IOException;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.driver.FloraOnGraph;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.NodeTypes;

public class Author extends GeneralNodeWrapper {
	public AuthorVertex baseNode;
	private VertexEntity<AuthorVertex> vertexEntity=null;

	public Author(Integer idAut, String name, String email, String acronym,String username,Integer level) {
		this.baseNode=new AuthorVertex(idAut, name, email, acronym, username, level);
		super.baseNode=this.baseNode;
	}
	
	public Author(FloraOnGraph graph,Integer idAut, String name, String email, String acronym,String username,Integer level) throws ArangoException {
		this.baseNode=new AuthorVertex(idAut, name, email, acronym, username, level);
		super.baseNode=this.baseNode;
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.author.toString(), this.baseNode, false);
		super.baseNode._id=this.vertexEntity.getDocumentHandle();
	}

	public Author(FloraOnGraph graph, AuthorVertex aut) {
		this.baseNode=aut;
		super.baseNode=this.baseNode;
		this.graph=graph;
	}

	@Override
	public void saveToDB() throws IOException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
