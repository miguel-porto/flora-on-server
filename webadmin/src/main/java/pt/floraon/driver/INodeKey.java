package pt.floraon.driver;

import pt.floraon.driver.Constants.DocumentType;

public interface INodeKey {
	public String toString();
	public String getID();
	/**
	 * Gets the key, which is not necessarily the same as the ID
	 * @return
	 */
	public String getDBKey();
	public String getColletion();
	public DocumentType getDocType();
}
