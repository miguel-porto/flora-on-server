package pt.floraon.entities;

import java.io.IOException;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.NodeTypes;

public class Author extends AuthorVertex implements VertexWrapper {
	private VertexEntity<AuthorVertex> vertexEntity=null;
	private FloraOnGraph graph;
	private Boolean dirty=false;

	public Author(Integer idAut, String name, String email, String acronym,String username,Integer level) {
		super(idAut, name, email, acronym, username, level);
	}
	
	public Author(FloraOnGraph graph,Integer idAut, String name, String email, String acronym,String username,Integer level) throws ArangoException {
		super(idAut, name, email, acronym, username, level);
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.author.toString(), new AuthorVertex(this), false);
		super._id=this.vertexEntity.getDocumentHandle();
	}

	public Author(FloraOnGraph graph, AuthorVertex aut) {
		super(aut);
		this.graph=graph;
	}

	@Override
	public void saveToDB() throws IOException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
