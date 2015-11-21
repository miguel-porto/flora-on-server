package pt.floraon.entities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.arangodb.ArangoException;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.results.GraphUpdateResult;
import pt.floraon.server.Constants.AllRelTypes;

public abstract class GeneralNodeWrapper {
	protected FloraOnGraph graph;
	protected Boolean dirty;
	protected GeneralDBNode baseNode;
	
	abstract void saveToDB() throws IOException, ArangoException;

	public String getID() {
		return baseNode._id;
	}
	
	public GeneralDBNode getNode() {
		return this.baseNode;
	}

	/**
	 * Creates a relationship of any type
	 * @param parent
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws ArangoException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public GraphUpdateResult createRelationshipTo(GeneralDBNode parent,AllRelTypes type) throws IOException, ArangoException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(baseNode._id==null) throw new IOException("Node "+baseNode._id+" not attached to DB");

		// checks whether there is already a PART_OF relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseNode._id,parent._id,type.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			return GraphUpdateResult.fromHandles(this.graph, new String[] {
				this.graph.driver.createEdge(type.toString(), type.getEdge(), baseNode._id, parent._id, false, false).getDocumentHandle()
				,baseNode._id,parent._id
			});
		} else return GraphUpdateResult.emptyResult();
	}
	
	/**
	 * Sets in the DB this node as PART_OF another node. Only adds a new relation if it doesn't exist. 
	 * @param parent
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setPART_OF(GeneralDBNode parent) throws ArangoException, IOException {
		if(baseNode._id==null) throw new IOException("Node "+baseNode._id+" not attached to DB");

		// checks whether there is already a PART_OF relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseNode._id,parent._id,AllRelTypes.PART_OF.toString());
		
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
		
		if(nrel==0) {
			this.graph.driver.createEdge(AllRelTypes.PART_OF.toString(), new PART_OF(true), baseNode._id, parent._id, false, false);
			return 1;
		} else return 0;
	}
	
}
