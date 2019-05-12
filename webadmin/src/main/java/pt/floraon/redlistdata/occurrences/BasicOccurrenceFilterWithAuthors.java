package pt.floraon.redlistdata.occurrences;

import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.IPolygonTheme;
import pt.floraon.occurrences.entities.Inventory;

import java.util.HashSet;
import java.util.Set;

/**
 * Adds ability to filter occurrences from givn maintainers.
 */
public class BasicOccurrenceFilterWithAuthors extends BasicOccurrenceFilter {
    private Set<String> fromAuthors;

    public BasicOccurrenceFilterWithAuthors(Integer minimumYear, Integer maximumYear, boolean includeDoubtful
            , IPolygonTheme clippingPolygon) {
        super(minimumYear, maximumYear, includeDoubtful, clippingPolygon);
    }

    public BasicOccurrenceFilterWithAuthors(Integer minimumYear, Integer maximumYear, boolean includeDoubtful
            , IPolygonTheme clippingPolygon, Set<String> fromAuthors) {
        super(minimumYear, maximumYear, includeDoubtful, clippingPolygon);
        this.fromAuthors = new HashSet<>();
        for(String author : fromAuthors)
            if(author != null) this.fromAuthors.add(author.trim().toLowerCase());
    }

    @Override
    public boolean enter(Inventory so) {
        boolean enter = super.enter(so);
        // format: enter &= !(<excluding condition>);
//        Log.info(so.getMaintainer().trim().toLowerCase());
        enter &= !(StringUtils.isStringEmpty(so.getMaintainer())
                || !this.fromAuthors.contains(so.getMaintainer()));
        return enter;
    }
}