package pt.floraon.server;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.function.ThrowingRunnable;
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

        te = new TaxonName("Cistus ladanifer sulcatus");
        assertEquals("Cistus", te.getGenus());
        assertEquals("ladanifer", te.getSpecificEpithet());
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("sulcatus", te.getInfraRanks().get(0).getInfraTaxon());

        assertThrows(TaxonomyException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                TaxonName te = new TaxonName("Cistus ladanifer sulcatus subsp. dummy");
            }
        });

        te = new TaxonName("Cistus ladanifer sulcatus (Demoly) P.Monts. sensu Flora Iberica");
        assertEquals("Cistus", te.getGenus());
        assertEquals("ladanifer", te.getSpecificEpithet());
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("sulcatus", te.getInfraRanks().get(0).getInfraTaxon());
        assertEquals("(Demoly) P.Monts.", te.getInfraRanks().get(0).getInfraAuthor());
        assertEquals("Flora Iberica", te.getInfraRanks().get(0).getInfraSensu());

        te = new TaxonName("Cistus ladanifer sulcatus (Demoly) P.Monts. var. dummy sensu Flora Iberica");
        assertEquals("Cistus", te.getGenus());
        assertEquals("ladanifer", te.getSpecificEpithet());
        assertEquals(2, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("sulcatus", te.getInfraRanks().get(0).getInfraTaxon());
        assertEquals("(Demoly) P.Monts.", te.getInfraRanks().get(0).getInfraAuthor());
        assertNull(te.getInfraRanks().get(0).getInfraSensu());
        assertEquals("var.", te.getInfraRanks().get(1).getInfraRank());
        assertEquals("dummy", te.getInfraRanks().get(1).getInfraTaxon());
        assertEquals("Flora Iberica", te.getInfraRanks().get(1).getInfraSensu());
        assertNull(te.getInfraRanks().get(1).getInfraAuthor());

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
        assertEquals("Klasea boetica (Boiss. ex DC.) Holub subsp. lusitanica (Cantó) Cantó & Rivas Mart var. sampaiana (Cantó) Cantó", te.toString());

    }
}
