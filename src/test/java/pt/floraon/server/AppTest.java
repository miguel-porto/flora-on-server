package pt.floraon.server;

import com.arangodb.ArangoException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import pt.floraon.driver.FloraOnGraph;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testApp() {
    	// TODO: record upload test!!!
    	FloraOnGraph fog=null;
    	try {
			fog=new FloraOnGraph("flora");
		} catch (ArangoException e2) {
			fail(e2.getErrorMessage());
		}

    	if(fog==null) fail("Unable to create database.");
/*
    	try {
			fog.dbDataUploader.uploadTaxonomyListFromStream(fog.getClass().getResourceAsStream("/taxonomia_full_novo.csv"), false);
			assertEquals(5593,fog.dbSpecificQueries.getNumberOfNodesInCollection(NodeTypes.taxent));
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (ArangoException e) {
			fail(e.getErrorMessage());
		}
    	*/
  /*      try {
        	System.out.println("Importing "+input.getFile());
        	fog.getDataUploader().uploadTaxonomyListFromFile(input.getFile(),false);
        	//fog.getDataUploader().uploadTaxonomyListFromFile("/media/miguel/Brutal/SPB/Flora-On/Taxonomia/Grafo/stepping_stones.csv",false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
    }
}
