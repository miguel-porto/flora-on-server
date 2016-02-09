package pt.floraon.arangodriver;

import java.lang.reflect.InvocationTargetException;

import com.arangodb.ArangoException;

import pt.floraon.driver.Constants;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.INodeWrapper;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.entities.GeneralDBEdge;
import pt.floraon.entities.PART_OF;
import pt.floraon.results.GraphUpdateResult;

/**
 * Wraps a node to perform writing operations on it
 * @author miguel
 *
 */
public class NodeWrapperDriver extends NodeWorkerDriver implements INodeWrapper {
	protected INodeKey node;

	public NodeWrapperDriver(FloraOn driver, INodeKey node) {
		super(driver);
		if(node==null) throw new RuntimeException("Must provide a node");
		this.node=node;
	}

	@Override
	public int setPART_OF(INodeKey parent) throws FloraOnException {
		return this.createRelationshipTo(parent, new PART_OF(true));
	}
	
	@Override
	public GraphUpdateResult createRelationshipTo(INodeKey parent,RelTypes type) throws FloraOnException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String baseId=node.toString();
		String parentId=parent.toString();

		// checks whether there is already a relation of this type between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseId,parentId,type.toString());
		
		try {
			Integer nrel=dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
			
			if(nrel==0) {
				if(type.getDirectionality().equals(Constants.Directionality.UNIDIRECTIONAL)) {	// TODO: do we need bidirectional links yet? 
					return new GraphUpdateResult(driver, new String[] {
						dbDriver.createEdge(type.toString(), type.getEdge(), baseId, parentId, false, false).getDocumentHandle()
						,baseId,parentId
					});
				} else {	// in bidirectional links we add two links so that we don't have to worry abound directionality in queries
					return new GraphUpdateResult(driver, new String[] {
						dbDriver.createEdge(type.toString(), type.getEdge(), baseId, parentId, false, false).getDocumentHandle()
						,dbDriver.createEdge(type.toString(), type.getEdge(), parentId, baseId, false, false).getDocumentHandle()
						,baseId,parentId
					});
				}
			} else return GraphUpdateResult.emptyResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException {
		String baseId=node.toString();
		String parentId=parent.toString();

		// checks whether there is already a relation of this type between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,baseId,parentId,edge.getType().toString());
		
		try {
			Integer nrel=dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();	
			if(nrel==0) {
				dbDriver.createEdge(edge.getType().toString(), edge, baseId, parentId, false, false);
				return 1;
			} else return 0;
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

}
