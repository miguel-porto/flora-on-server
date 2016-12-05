package pt.floraon.driver;

import pt.floraon.morphology.entities.ATTRIBUTE_OF;
import pt.floraon.driver.entities.GeneralDBEdge;

public class GAttributeWrapper extends BaseFloraOnDriver implements IAttributeWrapper {
	private INodeWrapper NWD;
	public GAttributeWrapper(IFloraOn driver, INodeKey node) throws FloraOnException {
		super(driver);
		NWD=(INodeWrapper) driver.wrapNode(node);
	}

	@Override
	public int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException {
		return NWD.createRelationshipTo(parent, edge);
	}

	@Override
	public int setAttributeOfCharacter(INodeKey character) throws FloraOnException {
		return createRelationshipTo(character, new ATTRIBUTE_OF());
		/*
		// checks whether there is already a HAS_QUALITY relation between these two nodes
		String query=String.format(
			"FOR e IN %3$s FILTER e._from=='%1$s' && e._to=='%2$s' COLLECT WITH COUNT INTO l RETURN l"
			,node.getID(),character.getID(),RelTypes.ATTRIBUTE_OF.toString());
		
		Integer nrel;
		try {
			nrel = dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
			if(nrel==0) {
				dbDriver.createEdge(RelTypes.ATTRIBUTE_OF.toString(), new ATTRIBUTE_OF(), node.getID(), character.getID(), false, false);
				return 1;
			} else return 0;
		} catch (ArangoException e) {
			throw new FloraOnException(e.getErrorMessage());
		}	
		*/
	}
}
