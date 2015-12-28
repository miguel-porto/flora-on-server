package pt.floraon.entities;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;

public class GeneralNodeWrapperImpl extends GeneralNodeWrapper {
	
	public GeneralNodeWrapperImpl(FloraOnDriver graph, GeneralDBNode node) {
		this.baseNode=node;
		this.graph=graph;
	}

	@Override
	void commit() throws FloraOnException, ArangoException {
		// TODO Auto-generated method stub
		
	}

}
