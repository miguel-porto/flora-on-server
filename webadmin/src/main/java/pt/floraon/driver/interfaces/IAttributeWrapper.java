package pt.floraon.driver.interfaces;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.entities.GeneralDBEdge;

public interface IAttributeWrapper {
	public int setAttributeOfCharacter(INodeKey character) throws FloraOnException;
	public int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException;
}
