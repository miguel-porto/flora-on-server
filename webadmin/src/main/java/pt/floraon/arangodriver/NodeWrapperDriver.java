package pt.floraon.arangodriver;

import java.lang.reflect.InvocationTargetException;

import com.arangodb.ArangoException;

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

	public NodeWrapperDriver(FloraOn driver, INodeKey node) throws FloraOnException {
		super(driver);
		if(node==null) throw new FloraOnException("Must provide a node");
		this.node=node;
	}

	@Override
	public int setPART_OF(INodeKey parent) throws FloraOnException {
		return this.createRelationshipTo(parent, new PART_OF());
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
				return new GraphUpdateResult(driver, new String[] {
					dbDriver.createEdge(type.toString(), type.getEdge(), baseId, parentId, false).getDocumentHandle()
					,baseId,parentId
				});
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
				dbDriver.createEdge(edge.getType().toString(), edge, baseId, parentId, false);
				return 1;
			} else return 0;
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

}
