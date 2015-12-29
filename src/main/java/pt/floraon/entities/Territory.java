package pt.floraon.entities;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.NodeTypes;

public class Territory extends GeneralNodeWrapper {
	public TerritoryVertex baseNode;
	
	private Territory(String name,String shortName) {
		baseNode.name=name;
		baseNode.shortName=shortName;
	}
	
	public static Territory newFromName(FloraOnDriver driver,String name,String shortName) throws TaxonomyException, ArangoException {
		Territory out=new Territory(name, shortName);
		out.graph=driver;
		VertexEntity<TerritoryVertex> tmp=driver.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.territory.toString(), out.baseNode, false);
		out.baseNode._id=tmp.getDocumentHandle();
		out.baseNode._key=tmp.getDocumentKey();
		return out;
	}
	
	@Override
	void commit() throws FloraOnException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
