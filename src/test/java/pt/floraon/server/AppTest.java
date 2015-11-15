package pt.floraon.server;

import java.io.IOException;
import java.net.URL;

import com.arangodb.ArangoException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.dbworker.QueryException;
import pt.floraon.dbworker.TaxEntName;
import pt.floraon.entities.TaxEnt;
import pt.floraon.server.Constants.TaxonRanks;

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

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
//    	URL input = getClass().getResource("taxonomia_full_novo.csv");
/*    	FloraOnGraph fog;
    	try {
			fog=new FloraOnGraph("flora");
		} catch (ArangoException e2) {
			return;
		}
    	
    	TaxEntName ten=new TaxEntName();
    	ten.author="L.";
    	ten.name="TEST NODE";
    	ten.rank=TaxonRanks.SPECIES;
    	
    	try {
    		TaxEnt in=new TaxEnt(fog,ten,true);
    		TaxEntName tmp=new TaxEntName();
    		tmp.name=ten.name;
    		TaxEnt tmp1=fog.findTaxEnt(tmp);
    		assertEquals(tmp1,in);
			//new TaxEnt(fog,ten,true);
			
		} catch (ArangoException e) {
			fail(e.getErrorMessage());
		} catch (QueryException e) {
			fail(e.getMessage());
		}*/
    	
  /*      try {
        	System.out.println("Importing "+input.getFile());
        	fog.getDataUploader().uploadTaxonomyListFromFile(input.getFile(),false);
        	//fog.getDataUploader().uploadTaxonomyListFromFile("/media/miguel/Brutal/SPB/Flora-On/Taxonomia/Grafo/stepping_stones.csv",false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
    	
    	assertTrue(true);
        
    }
}
