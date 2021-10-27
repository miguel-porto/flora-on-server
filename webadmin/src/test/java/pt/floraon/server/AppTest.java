package pt.floraon.server;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.function.ThrowingRunnable;
import pt.floraon.driver.Constants;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.occurrences.fields.parsers.DateParser;
import pt.floraon.taxonomy.entities.TaxonName;

public class AppTest
{
    @Test
    public void taxonNameParsingTest() throws TaxonomyException {
        TaxonName te;

        te = new TaxonName("Cistus ladanifer subsp. sulcatus");
        assertEquals("Cistus", te.getGenus());
        assertEquals("ladanifer", te.getSpecificEpithet());
        assertNull(te.getSensu());
        assertNull(te.getAnnotation());
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("sulcatus", te.getInfraRanks().get(0).getInfraTaxon());

        te = new TaxonName("  Armeria   langei  subsp.   langei    [pop. portuguesas]   ");
        assertEquals("Armeria", te.getGenus());
        assertEquals("langei", te.getSpecificEpithet());
        assertNull(te.getSensu());
        assertEquals("pop. portuguesas", te.getLastAnnotation());
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("langei", te.getInfraRanks().get(0).getInfraTaxon());
        assertEquals("pop. portuguesas", te.getInfraRanks().get(0).getInfraAnnotation());

        // test non-ASCII space characters
        te = new TaxonName("Forficula decipiens");
        assertEquals("Forficula", te.getGenus());
        assertEquals("decipiens", te.getSpecificEpithet());
        assertEquals(0, te.getInfraRanks().size());

        te = new TaxonName("Cistus ladanifer sulcatus");
        assertEquals("Cistus", te.getGenus());
        assertEquals("ladanifer", te.getSpecificEpithet());
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("sulcatus", te.getInfraRanks().get(0).getInfraTaxon());

        te = new TaxonName("Tolpis barbata sensu Franco & Rocha Afonso");
        assertEquals("Tolpis", te.getGenus());
        assertEquals("barbata", te.getSpecificEpithet());
        assertNull(te.getLastAuthor());
        assertEquals(0, te.getInfraRanks().size());
        assertEquals("Franco & Rocha Afonso", te.getSensu());
        assertEquals("Tolpis barbata sensu Franco & Rocha Afonso", te.getCanonicalName(true));

        te = new TaxonName("Pedipes dohrni d’Ailly");
        assertEquals("Pedipes", te.getGenus());
        assertEquals("dohrni", te.getSpecificEpithet());
        assertEquals(0, te.getInfraRanks().size());
        assertEquals("d’Ailly", te.getLastAuthor());

        te = new TaxonName("Chrysanthemum coronarium var. discolor d'Urv.");
        assertEquals("Chrysanthemum", te.getGenus());
        assertEquals("coronarium", te.getSpecificEpithet());
        assertNull(te.getSensu());
        assertNull(te.getAnnotation());
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("var.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("discolor", te.getInfraRanks().get(0).getInfraTaxon());
        assertEquals("d'Urv.", te.getLastAuthor());

        te = new TaxonName("Leptoneta berlandi Machado & Ribera, 1986");
        assertEquals("Leptoneta", te.getGenus());
        assertEquals("berlandi", te.getSpecificEpithet());
        assertNull(te.getSensu());
        assertNull(te.getAnnotation());
        assertEquals(0, te.getInfraRanks().size());
        assertEquals("Machado & Ribera, 1986", te.getLastAuthor());

        te = new TaxonName("Tolpis barbata (L.) Gaertn. sensu Franco & Rocha Afonso");
        assertEquals("Tolpis", te.getGenus());
        assertEquals("barbata", te.getSpecificEpithet());
        assertEquals("(L.) Gaertn.", te.getLastAuthor());
        assertEquals(0, te.getInfraRanks().size());
        assertEquals("Franco & Rocha Afonso", te.getSensu());
        assertEquals("Tolpis barbata sensu Franco & Rocha Afonso", te.getCanonicalName(true));

        assertThrows(TaxonomyException.class, new TaxonNameExceptionThrower("Asphodelus"));
        assertThrows(TaxonomyException.class, new TaxonNameExceptionThrower("Cistus ladanifer sulcatus subsp. dummy"));
        assertThrows(TaxonomyException.class, new TaxonNameExceptionThrower("Festuca rubra subsp. arenaria ou ..."));
        assertThrows(TaxonomyException.class, new TaxonNameExceptionThrower("Olea europaea var. sylvestris, Quercus rotundifolia"));
        assertThrows(TaxonomyException.class, new TaxonNameExceptionThrower("Quercus robur subsp. estremadurensis x Q. alentejana"));

        te = new TaxonName("Cistus ladanifer sulcatus (Demoly) P.Monts. sensu Flora Iberica");
        assertEquals("Cistus", te.getGenus());
        assertEquals("ladanifer", te.getSpecificEpithet());
        assertEquals(1, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("sulcatus", te.getInfraRanks().get(0).getInfraTaxon());
        assertEquals("(Demoly) P.Monts.", te.getInfraRanks().get(0).getInfraAuthor());
        assertEquals("Flora Iberica", te.getInfraRanks().get(0).getInfraSensu());
        assertEquals("Cistus ladanifer subsp. sulcatus sensu Flora Iberica", te.getCanonicalName(true));

        // ignore minor typos
        te = new TaxonName("Trifolium pratense subsp.... pratense var.. pratense");
        assertEquals("Trifolium", te.getGenus());
        assertEquals("pratense", te.getSpecificEpithet());
        assertEquals(2, te.getInfraRanks().size());
        assertEquals("subsp.", te.getInfraRanks().get(0).getInfraRank());
        assertEquals("pratense", te.getInfraRanks().get(0).getInfraTaxon());
        assertNull(te.getInfraRanks().get(0).getInfraSensu());
        assertNull(te.getInfraRanks().get(0).getInfraAuthor());
        assertEquals("var.", te.getInfraRanks().get(1).getInfraRank());
        assertEquals("pratense", te.getInfraRanks().get(1).getInfraTaxon());
        assertNull(te.getInfraRanks().get(1).getInfraSensu());
        assertNull(te.getInfraRanks().get(1).getInfraAuthor());

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
        assertEquals("Cistus ladanifer subsp. sulcatus var. dummy sensu Flora Iberica", te.getCanonicalName(true));

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
        assertEquals("Klasea boetica subsp. lusitanica var. sampaiana", te.getCanonicalName(true));

    }

    @Test
    public void dateParsingTest() throws TaxonomyException {
        Integer[] date;

        date = DateParser.parseDate("23-5-2013");
        assertEquals((Integer) 23, date[0]);
        assertEquals((Integer) 5, date[1]);
        assertEquals((Integer) 2013, date[2]);
        assertEquals(Constants.NODATA_INT, date[3]);
        assertEquals(Constants.NODATA_INT, date[4]);

        date = DateParser.parseDate("2100-12-2");
        assertEquals((Integer) 2, date[0]);
        assertEquals((Integer) 12, date[1]);
        assertEquals((Integer) 2100, date[2]);
        assertEquals(Constants.NODATA_INT, date[3]);
        assertEquals(Constants.NODATA_INT, date[4]);

        date = DateParser.parseDate("2100/12/2 15:12");
        assertEquals((Integer) 2, date[0]);
        assertEquals((Integer) 12, date[1]);
        assertEquals((Integer) 2100, date[2]);
        assertEquals((Integer) 15, date[3]);
        assertEquals((Integer) 12, date[4]);

        date = DateParser.parseDate("4-1-1980 11:10:59");
        assertEquals((Integer) 4, date[0]);
        assertEquals((Integer) 1, date[1]);
        assertEquals((Integer) 1980, date[2]);
        assertEquals((Integer) 11, date[3]);
        assertEquals((Integer) 10, date[4]);

    }

        /**
         * Try to throw an exception with given taxon name
         */
    static class TaxonNameExceptionThrower implements ThrowingRunnable {
        private final String name;

        TaxonNameExceptionThrower(String name) {
            this.name = name;
        }

        @Override
        public void run() throws Throwable {
            new TaxonName(this.name);
        }
    }
}
