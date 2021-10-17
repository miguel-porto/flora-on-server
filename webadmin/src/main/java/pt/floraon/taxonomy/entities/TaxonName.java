package pt.floraon.taxonomy.entities;

import jline.internal.Log;
import org.apache.commons.lang.WordUtils;
import pt.floraon.driver.Constants;
import pt.floraon.driver.TaxonomyException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a parsed _binomial_ taxon name with intercalated authorship, annotations and sensu
 * THIS IS THE MAIN CLASS FOR PARSING TAXON NAMES
 * Created by miguel on 22-12-2019.
 */
public class TaxonName {
    private String genus, specificEpithet, author, annotation, sensu;
    private List<InfraRank> infraRanks;
    /**
     * Matches a taxon name, given as a canonical name: no author, no anything else than
     * Genus species (subsp. infrarank)... does not accept uninomial names.
     */
    private transient static Pattern completeName = Pattern.compile(
            "^ *(?<subrank>subgen.? +)?(?<genus>[A-Z][a-zç]+)" +
                    "(?: +(?<species>[a-zç-]+)(?: +(?!sensu )(?<subspecies>[a-zç-]+))?(?: +(?<author> *[A-ZÁÉÍÓÚ(][^\\[\\]{}]+?)?)?)?" +
                    "(?: +\\[(?<annot>[\\w çãõáàâéêíóôú]+)])?(?: +sensu +(?<sensu>[^\\[\\]]+))?" +
                    "(?: +(?<rest>(subsp|var|f|ssp|subvar|forma)\\.* .*))?$");

/*
    private transient static Pattern completeName = Pattern.compile(
            "^ *(?<subrank>subgen.? +)?(?<genus>[A-Z][a-zç]+)" +
                    "(?: +(?<species>[a-zç-]+)(?: +(?<author> *[A-ZÁÉÍÓÚ(][^\\[\\]{}]+?)?)?)?" +
                    "(?: +\\[(?<annot>[\\w çãõáàâéêíóôú]+)])?(?: +sensu +(?<sensu>[^\\[\\]]+))?" +
                    "(?: +(?<rest>(subsp|var|f|ssp|subvar|forma)\\.? .*))?$");
*/

    private final transient static Pattern infraTaxa = Pattern.compile(
            " *(?<rank>subsp|var|f|ssp|subvar|forma)\\.* +(?<infra>[a-zç-]+)(?: +(?<author> *[A-ZÁÉÍÓÚd(][^\\[\\]{}]+?)?)?" +
                    "(?: +\\[(?<annot>[\\w .çãõáàâéêíóôú]+)])?(?: +sensu +(?<sensu>[^\\[\\]]+?))? *" +
                    "(?= +(?:subsp|var|f|ssp|subvar|forma)\\.* +|$)");  // a position look-ahead to ensure that the regex decomposes each infrataxon fully

/*
    private transient static Pattern completeName = Pattern.compile(
            "^ *(?<subrank>subgen.? +)?(?<genus>[A-Z][a-zç]+)(?: +(?<species>[a-zç-]+)" +
                    "(?: +(?<author> *[A-ZÁÉÍÓÚ(][^\\[\\]{}]+?)?)?" +
                    ")?(?: +\\[(?<annot>[\\w çãõáàâéêíóôú]+)])?(?: +sensu +(?<sensu>[^\\[\\]]+))?" +
                    "(?: +(?<rest>(subsp|var|f|ssp|subvar|forma)\\.? .*))?$");

    private transient static Pattern infraTaxa = Pattern.compile(
            " *(?:(?<rank>subsp|var|f|ssp|subvar|forma)\\.? +)?(?<infra>[a-zç-]+)(?: +(?<author> *[A-ZÁÉÍÓÚ(][^\\[\\]{}]+?)?)?" +
            "(?: +\\[(?<annot>[\\w çãõáàâéêíóôú]+)])?(?: +sensu +(?<sensu>[^\\[\\]]+?))?" +
            "(?= +(?:subsp|var|f|ssp|subvar|forma)\\.? +|$)");  // a position look-ahead to ensure that the regex decomposes each infrataxon fully
*/

    public TaxonName() {}

    public TaxonName(String verbatimName) throws TaxonomyException {
        // get rid of strange spaces
        verbatimName = verbatimName.replaceAll("[ \\t\\xA0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000]", " ");

        Matcher m = completeName.matcher(verbatimName);
        Log.info("  Verb: " + verbatimName);
        String previousEpithet, previousAuthor;
        if(m.find()) {
            this.genus = WordUtils.capitalize(m.group("genus"));
            this.specificEpithet = m.group("species");
            if(this.specificEpithet == null) {
                throw new TaxonomyException(String.format("Name must be, at least, binomial (found for '%s')", verbatimName));
/*
                Log.info("    Canonical: G=" + genus);
                return;
*/
            }
            this.author = m.group("author");
            this.annotation = m.group("annot");
            this.sensu = m.group("sensu");
            String rest = m.group("rest");
            previousEpithet = this.specificEpithet;
            previousAuthor = this.author;
            if(m.group("subspecies") != null) {     // this is a trinomial name, so assume is subspecies
                InfraRank tmp = new InfraRank("subsp.", m.group("subspecies"), this.author, this.annotation, this.sensu);
                this.sensu = null;  // we add the sensu to the infrataxon, so remove it from the species.
                this.infraRanks = new ArrayList<>();
                this.infraRanks.add(tmp);
                Log.info(String.format("      Infra: rank=%s; infra=%s; auth=%s; annot=%s; sensu=%s", tmp.getInfraRank(), tmp.getInfraTaxon(), tmp.getInfraAuthor(),
                        tmp.getInfraAnnotation(), tmp.getInfraSensu()));
            }
            if (rest != null) {
                Log.info(String.format("      Canonical: G=%s; S=%s; auth=%s; rest=%s", genus, specificEpithet, author, rest));
                if(this.infraRanks == null)
                    this.infraRanks = new ArrayList<>();
                Matcher m1 = infraTaxa.matcher(rest);

                while (m1.find()) {
                    if (m1.group("infra") != null) {
                        InfraRank tmp = new InfraRank(m1.group("rank"), m1.group("infra"),
                                (m1.group("author") == null && m1.group("infra").equals(previousEpithet)) ?
                                        previousAuthor : m1.group("author"),
                                m1.group("annot"), m1.group("sensu"));

                        for(InfraRank ir : this.infraRanks)
                            if(tmp.getInfraRank().equals(ir.getInfraRank())) throw new TaxonomyException(String.format("Duplicated infrarank: %s", tmp.getInfraRank()));

                        previousEpithet = tmp.infraTaxon;
                        previousAuthor = tmp.infraAuthor;

                        Log.info(String.format("      Infra: rank=%s; infra=%s; auth=%s; annot=%s; sensu=%s", m1.group("rank"), m1.group("infra"), m1.group("author"),
                                m1.group("annot"), m1.group("sensu")));
                        this.infraRanks.add(tmp);
                    }
                }
                if(this.infraRanks.size() == 0) {     // has a trailing substring, but could not be parsed
                    Log.info("      INVALID NAME!");
                    throw new TaxonomyException("Could not parse this name: " + verbatimName);
                }
            } else
                Log.info(String.format("    Canonical: G=%s; S=%s; auth=%s; annot=%s; sensu=%s", genus, specificEpithet, author, annotation, sensu));
        } else {
            Log.info("      INVALID NAME!");
            throw new TaxonomyException("Could not parse this name: " + verbatimName);
        }
    }

    /**
     * @param rankOrder The rank order at which to prune
     * @return A clipped TaxonName at the given rank order.
     */
    public TaxonName truncateAtInfraRank(int rankOrder) {
        TaxonName out = new TaxonName();
        out.genus = this.genus;
        out.specificEpithet = this.specificEpithet;
        out.author = this.author;
        out.annotation = this.annotation;
        out.sensu = this.sensu;
        if(rankOrder == 0) {
            out.infraRanks = null;
        } else {
            if(this.infraRanks != null) {
                if(rankOrder < this.infraRanks.size()) {
                    out.infraRanks = new ArrayList<>();
                    for(int i=0; i<rankOrder; i++)
                        out.infraRanks.add(this.infraRanks.get(i));
//                    out.infraRanks = this.infraRanks.subList(0, rankOrder - 1);
                } else
                    out.infraRanks = this.infraRanks;
            }
        }
        return out;
    }

    public String getGenus() {
        return genus;
    }

    public String getSpecificEpithet() {
        return specificEpithet;
    }

    public List<InfraRank> getInfraRanks() {
        return infraRanks == null ? new ArrayList<InfraRank>() : this.infraRanks;
    }

    /**
     * @return The deepest rank found in this taxon
     */
    public Constants.TaxonRanks getTaxonRank() {
        if(getInfraRanks() == null || getInfraRanks().size() == 0) {
            if(getSpecificEpithet() == null)
                return null;
            else
                return Constants.TaxonRanks.SPECIES;
        } else
            return Constants.TaxonRanks.getRankFromShortname(
                    getInfraRanks().get(getInfraRanks().size() - 1).getInfraRank()
            );
    }

    public String getInfraRanksAsString(boolean htmlFormatted, boolean canonical, boolean withAnnotations) {
        if(infraRanks == null)
            return "";
        else {
            StringBuilder sb = new StringBuilder();
            for (InfraRank ir : this.infraRanks) {
                if(!canonical)
                    sb.append(" ").append(ir.toString(htmlFormatted));
                else
                    sb.append(" ").append(ir.getCanonicalInfrarank(htmlFormatted, withAnnotations));
            }
            return sb.toString().trim();
        }
    }

    public String getLastAuthor() {
        if(this.infraRanks == null || this.infraRanks.size() == 0)
            return this.author;
        else
            return this.infraRanks.get(this.infraRanks.size() - 1).infraAuthor;
    }

    public String getAuthor(int n) {
        if(n == 0)
            return this.author;
        else {
            if(this.infraRanks == null || this.infraRanks.size() == 0)
                return null;
            else {
                if(n > this.infraRanks.size())
                    return null;
                else
                    return this.infraRanks.get(n - 1).infraAuthor;
            }
        }
    }

    public String getAnnotation() {
        return this.annotation;
    }

    public String getSensu() {
        return this.sensu;
    }

    public String getLastAnnotation() {
        if(this.infraRanks == null || this.infraRanks.size() == 0)
            return this.annotation;
        else
            return this.infraRanks.get(this.infraRanks.size() - 1).infraAnnotation;
    }

    public String getLastSensu() {
        if(this.infraRanks == null || this.infraRanks.size() == 0)
            return this.sensu;
        else
            return this.infraRanks.get(this.infraRanks.size() - 1).infraSensu;
    }

    public String getCanonicalName() {
        return this.getCanonicalName(false);
    }

    public String getCanonicalName(boolean withAnnotations) {
        StringBuilder sb = new StringBuilder(this.genus);
        if(this.specificEpithet == null) return sb.toString();
        sb.append(" ").append(this.specificEpithet);

        if(withAnnotations) {
            if(this.sensu != null) sb.append(" sensu ").append(this.sensu);
            if(this.annotation != null) sb.append(" [").append(this.annotation).append("]");
        }

        if(this.infraRanks != null) {
            sb.append(" ").append(this.getInfraRanksAsString(false, true, withAnnotations));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.genus);
        if(this.specificEpithet == null) return sb.toString();
        sb.append(" ").append(this.specificEpithet);
        if(this.author != null)
            sb.append(" ").append(this.author);
        if(this.annotation != null)
            sb.append(" [").append(this.annotation).append("]");
        if(this.sensu != null)
            sb.append(" sensu ").append(this.sensu);

        if(this.infraRanks != null) {
            sb.append(" ").append(this.getInfraRanksAsString(false, false, true));
        }
        return sb.toString();
    }

    public String toString(boolean htmlFormatted) {
        if(!htmlFormatted) return this.toString();
        StringBuilder sb = new StringBuilder("<i>" + this.genus);
        if(this.specificEpithet == null) {
            sb.append("</i>");
            return sb.toString();
        }
        sb.append(" ").append(this.specificEpithet).append("</i>");
        if(this.author != null)
            sb.append(" ").append(this.author);
        if(this.annotation != null)
            sb.append(" [").append(this.annotation).append("]");
        if(this.sensu != null)
            sb.append(" sensu ").append(this.sensu);

        if(this.infraRanks != null) {
            sb.append(" ").append(this.getInfraRanksAsString(true, false, true));
        }
        return sb.toString();
    }

    public static class InfraRank {
        private final String infraRank, infraTaxon, infraAuthor, infraAnnotation, infraSensu;

        public InfraRank(String infraRank, String infraTaxon, String infraAuthor, String infraAnnotation, String infraSensu) {
            // TODO we assume the default infrarank is subsp... is it ok?
            this.infraRank = infraRank == null ? "subsp." : (infraRank.endsWith(".") ? infraRank : (infraRank + "."));
            this.infraTaxon = infraTaxon;
            this.infraAuthor = infraAuthor;
            this.infraAnnotation = infraAnnotation;
            this.infraSensu = infraSensu;
        }

        public String getInfraRank() {
            return infraRank;
        }

        public String getInfraTaxon() {
            return infraTaxon;
        }

        public String getInfraAuthor() {
            return infraAuthor;
        }

        public String getInfraAnnotation() {
            return infraAnnotation;
        }

        public String getInfraSensu() {
            return infraSensu;
        }

        public String getCanonicalInfrarank(boolean htmlFormatted, boolean withAnnotations) {
            StringBuilder sb = new StringBuilder();
            if(this.infraRank != null)
                sb.append(this.infraRank).append(this.infraRank.endsWith(".") ? "" : ".").append(" ");

            if(!htmlFormatted)
                sb.append(this.infraTaxon);
            else
                sb.append("<i>").append(this.infraTaxon).append("</i>");

            if(withAnnotations) {
                if(this.infraSensu != null) sb.append(" sensu ").append(this.infraSensu);
                if(this.infraAnnotation != null) sb.append(" [").append(this.infraAnnotation).append("]");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if(this.infraRank != null)
                sb.append(this.infraRank).append(this.infraRank.endsWith(".") ? "" : ".").append(" ");
            sb.append(this.infraTaxon);

            if(this.infraAuthor != null)
                sb.append(" ").append(this.infraAuthor);

            if(this.infraAnnotation != null)
                sb.append(" [").append(this.infraAnnotation).append("]");

            if(this.infraSensu != null)
                sb.append(" sensu ").append(this.infraSensu);

            return sb.toString();
        }

        public String toString(boolean htmlFormatted) {
            if(!htmlFormatted) return this.toString();
            StringBuilder sb = new StringBuilder();
            if(this.infraRank != null)
                sb.append(this.infraRank).append(this.infraRank.endsWith(".") ? "" : ".").append(" ");
            sb.append("<i>").append(this.infraTaxon).append("</i>");

            if(this.infraAuthor != null)
                sb.append(" ").append(this.infraAuthor);

            if(this.infraAnnotation != null)
                sb.append(" [").append(this.infraAnnotation).append("]");

            if(this.infraSensu != null)
                sb.append(" <i>sensu</i> ").append(this.infraSensu);

            return sb.toString();
        }
    }
}
