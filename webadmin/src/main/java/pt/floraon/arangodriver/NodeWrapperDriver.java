package pt.floraon.arangodriver;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

import com.arangodb.ArangoDBException;

import pt.floraon.driver.*;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.entities.GeneralDBEdge;
import pt.floraon.taxonomy.entities.PART_OF;
import pt.floraon.driver.results.GraphUpdateResult;

/**
 * Wraps a node to perform writing operations on it
 * @author miguel
 *
 */
public class NodeWrapperDriver extends NodeWorkerDriver implements INodeWrapper {
	protected INodeKey node;

	public NodeWrapperDriver(IFloraOn driver, INodeKey node) throws FloraOnException {
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
			Integer nrel=database.query(query, null, null, Integer.class).next();
			if(nrel==0) {
				return new GraphUpdateResult(driver, new String[] {
//					dbDriver.createEdge(type.toString(), type.getEdge(), baseId, parentId, false).getDocumentHandle()
						database.graph(Constants.TAXONOMICGRAPHNAME).edgeCollection(type.toString()).insertEdge(type.getEdge(baseId, parentId)).getId()
						, baseId, parentId
				});
			} else return GraphUpdateResult.emptyResult();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException {
		String baseId=node.toString();
		String parentId=parent.toString();

		// checks whether there is already a relation of this type between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			, baseId, parentId, edge.getTypeAsString());
		
		try {
			Integer nrel=database.query(query,null,null,Integer.class).next();
			if(nrel==0) {
				edge.setFrom(baseId);
				edge.setTo(parentId);
				database.graph(Constants.TAXONOMICGRAPHNAME).edgeCollection(edge.getTypeAsString()).insertEdge(edge);
//				dbDriver.createEdge(edge.getTypeAsString().toString(), edge, baseId, parentId, false);
				return 1;
			} else return 0;
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

}
