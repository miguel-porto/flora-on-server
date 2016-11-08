package pt.floraon.driver;

import pt.floraon.driver.Constants.DocumentType;

public interface INodeKey {
	String toString();
	String getID();
	/**
	 * Gets the key, which is not necessarily the same as the ID
	 * @return
	 */
	String getDBKey();
	String getCollection();
	DocumentType getDocumentType();
}
