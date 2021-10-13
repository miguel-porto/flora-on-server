package pt.floraon.driver.interfaces;

import java.io.File;
import java.util.List;
import java.util.Properties;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.entities.GlobalSettings;
import pt.floraon.occurrences.CSVFileProcessor;
import pt.floraon.redlistdata.entities.RedListSettings;
import pt.floraon.taxonomy.entities.Territory;

/**
 * The constructor of a IFloraOn must either initialize a fresh new database, or check if there's one already functional.
 */
public interface IFloraOn {
	Object getDatabaseDriver();

	Object getDatabase();

	INodeWorker getNodeWorkerDriver();

	IQuery getQueryDriver();

	IListDriver getListDriver();

	CSVFileProcessor getCSVFileProcessor();

	IRedListDataDriver getRedListData();

	IOccurrenceDriver getOccurrenceDriver();

	IOccurrenceReportDriver getOccurrenceReportDriver();

	IAdministration getAdministration();

	IImageManagement getImageManagement();

	INodeWrapper wrapNode(INodeKey node) throws FloraOnException;

	ITaxEntWrapper wrapTaxEnt(INodeKey node) throws FloraOnException;

	IAttributeWrapper wrapAttribute(INodeKey node) throws FloraOnException;

	List<Territory> getChecklistTerritories();

	INodeKey asNodeKey(String id) throws FloraOnException;

	void reloadSettings();

	RedListSettings getRedListSettings(String territory);

	/**
	 * @return The global settings set in the database by the administrator
	 */
	GlobalSettings getGlobalSettings();

	void updateGlobalSettings(GlobalSettings newSettings);

	/**
	 * @return A reference to the properties file, where installation properties are defined (e.g. path to the image
	 * folder, database name and password, etc.)
	 */
	Properties getProperties();

	boolean hasFailed();

	String getErrorMessage();

	/**
	 * @return A reference to the folder where images are stored.
	 * @throws FloraOnException
	 */
	File getImageFolder();

	File getThumbsFolder();

	File getOriginalImageFolder();

	/**
	 * @return The context path where the webapp was deployed.
	 */
	String getContextPath();

	/**
	 * @return The default iNaturalist project from which to import regular user occurrences
	 */
	String getDefaultINaturalistProject();

	/**
	 * @return The default red list territory to be used as a reference (e.g. for threat categories)
	 */
	String getDefaultRedListTerritory();
}
