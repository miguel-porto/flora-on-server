package pt.floraon.geometry.gridmaps;

import pt.floraon.driver.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * A data object holding a list of taxa occurring in a square.
 */
public class ListOfTaxa implements SquareData {
    private final Set<String> taxa;

    ListOfTaxa(Set<String> taxa) {
        this.taxa = taxa;
    }

    @Override
    public int getNumber() {
        return this.taxa.size();
    }

    @Override
    public String getText() {
        return StringUtils.implode("\n", this.taxa.toArray(new String[0])).replace("&", "&amp;");
/*
        try {
            return URLEncoder.encode(StringUtils.implode("\n", this.taxa.toArray(new String[0])), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
*/
    }

    @Override
    public void add(Object o) {
        this.taxa.add((String) o);
    }
}
