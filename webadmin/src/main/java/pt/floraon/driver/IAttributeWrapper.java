package pt.floraon.driver;

import pt.floraon.entities.GeneralDBEdge;

public interface IAttributeWrapper {
	public int setAttributeOfCharacter(INodeKey character) throws FloraOnException;
	public int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException;
}
