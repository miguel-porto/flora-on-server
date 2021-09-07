package pt.floraon.server;

import org.junit.Test;
import static org.junit.Assert.*;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.taxonomy.entities.TaxonName;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    @Test
    public void taxonNameParsingTest() throws TaxonomyException {
        TaxonName te;
        te = new TaxonName("Cistus ladanifer subsp. sulcatus");
        assertEquals("Cistus", te.getGenus());
        assertEquals("ladanifer", te.getSpecificEpithet());
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("sulcatus", te.getInfraRanks().get(0).getInfraTaxon());

        te = new TaxonName("Klasea boetica (Boiss. ex DC.) Holub subsp. lusitanica (Cantó) Cantó & Rivas Mart");
        assertEquals("Klasea", te.getGenus());
        assertEquals("boetica", te.getSpecificEpithet());
        assertEquals("(Boiss. ex DC.) Holub", te.getAuthor(0));
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("lusitanica", te.getInfraRanks().get(0).getInfraTaxon());
        assertEquals("(Cantó) Cantó & Rivas Mart", te.getInfraRanks().get(0).getInfraAuthor());

        te = new TaxonName("Klasea boetica (Boiss. ex DC.) Holub subsp. lusitanica (Cantó) Cantó & Rivas Mart var. sampaiana (Cantó) Cantó");
        assertEquals("Klasea", te.getGenus());
        assertEquals("boetica", te.getSpecificEpithet());
        assertEquals("(Boiss. ex DC.) Holub", te.getAuthor(0));
        assertEquals(2, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("lusitanica", te.getInfraRanks().get(0).getInfraTaxon());
        assertEquals("(Cantó) Cantó & Rivas Mart", te.getInfraRanks().get(0).getInfraAuthor());
        assertEquals("var.", te.getInfraRanks().get(1).getInfraRank());
        assertEquals("sampaiana", te.getInfraRanks().get(1).getInfraTaxon());
        assertEquals("(Cantó) Cantó", te.getInfraRanks().get(1).getInfraAuthor());

//        te = new TaxonName("Cistus ladanifer sulcatus");

    }
    public void testApp() {

    	/*FloraOnArangoDriver fog=null;
    	try {
			fog=new FloraOnArangoDriver("flora");
		} catch (FloraOnException e2) {
			fail(e2.getMessage());
		}

    	if(fog==null) fail("Unable to create database.");*/
    	
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
