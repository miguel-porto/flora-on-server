package pt.floraon.redlistdata.entities;

import com.arangodb.velocypack.annotations.Expose;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.datatypes.Rectangle;
import pt.floraon.geometry.Polygon;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.servlets.RedListMainPages;

import java.util.*;

/**
 * The user settings for a given red list dataset.
 * TODO: here should go the protected areas and clipping polygon geojson!
 */
public class RedListSettings extends GeneralDBNode {
    private String territory;
    private boolean editionLocked = false;
    private Set<String> tagsEditionLocked = new HashSet<>();
    private Integer historicalThreshold, editionsLastNDays;
    private Set<String> unlockedSheets = new HashSet<>();
    private Rectangle mapBounds;
    private List<Polygon> baseMap;
    private Integer svgDivisor;
    @Expose(serialize = false, deserialize = false)
    private transient List<String> baseMapPathString;     // for caching purposes

    public Rectangle getMapBounds() {
        return mapBounds == null ? new Rectangle(0, 0, 0, 0) : mapBounds;
    }

    public void setMapBounds(long left, long right, long top, long bottom) {
        this.mapBounds = new Rectangle(left, right, top, bottom);
    }

    public List<Polygon> getBaseMap() {
        return this.baseMap == null ? Collections.<Polygon>emptyList() : this.baseMap;
    }

    /**
     * Returns the SVG path string of the basemap, already divided by the divisor.
     * @return
     */
    public List<String> getBaseMapPathString() {
        if(this.baseMapPathString == null) {
            this.baseMapPathString = new ArrayList<>();
            for (Polygon p : this.getBaseMap()) {
                this.baseMapPathString.add(p.toSVGPathString(this.getSvgMapDivisor()));
            }
        }
        return this.baseMapPathString;
    }

    /**
     * The divisor to be applied to map coordinates when exporting SVG. This is usually needed for projected coordinates
     * because the values are too large to be used in conventional SVG editing software.
     * @return
     */
    public Integer getSvgMapDivisor() {
        return this.svgDivisor == null ? 1 : this.svgDivisor;
    }

    public Integer getHistoricalThreshold() {
        return historicalThreshold == null ? 1990 : historicalThreshold;
    }

    public Integer getEditionsLastNDays() {
        return editionsLastNDays == null ? 20 : editionsLastNDays;
    }

    public String getTerritory() {
        return territory;
    }

    public Set<String> getUnlockedSheets() {
        return unlockedSheets;
    }

    public Set<String> getLockedTags() {
        return tagsEditionLocked;
    }

    public void lockEditionForTag(String tag) {
        this.tagsEditionLocked.add(tag);
    }

    public void unlockEditionForTaxon(String taxEntId) {
        this.unlockedSheets.add(taxEntId);
    }

    public void removeUnlockEditionException(String taxEntId) {
        this.unlockedSheets.remove(taxEntId);
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }

    public boolean isEditionGloballyLocked() {
        return editionLocked;
    }

    public boolean isEditionLocked(String taxEntId, String[] tagsOfThisTaxon) {
        return (editionLocked || !Collections.disjoint(Arrays.asList(tagsOfThisTaxon), tagsEditionLocked));
//                && !this.unlockedSheets.contains(taxEntId);
    }

    public boolean isEditionLocked(RedListDataEntity rlde) {
        return isEditionLocked(rlde.getTaxEntID(), rlde.getTags());
    }

    public boolean isSheetUnlocked(String taxEntId) {
        return this.unlockedSheets.contains(taxEntId);
    }

    public void setEditionLocked(boolean editionLocked) {
        this.editionLocked = editionLocked;
    }

    // TODO: this must be user configuration
    public PolygonTheme getClippingPolygon() {
        return new PolygonTheme(RedListMainPages.class
                .getResourceAsStream("/pt/floraon/redlistdata/servlets/PT_buffer.geojson"), null);
    }

    // TODO: this must be user configuration
    public PolygonTheme getProtectedAreas() {
        return new PolygonTheme(RedListMainPages.class
                .getResourceAsStream("/pt/floraon/redlistdata/servlets/SNAC.geojson"), "SITE_NAME");
    }

    // TODO: this must be user configuration
    /**
     * Gets a custom list of taxa defined by the user.
     * @param groupName
     * @return
     */
    public List<String> getTaxonGroup(String groupName) {
        switch (groupName) {
            case "olivais":
                return Arrays.asList("taxent/338146505630","taxent/338148078494","taxent/727429989271"
                        ,"taxent/335881057182","taxent/1489742363194","taxent/340986901406"
                        ,"taxent/335732159390","taxent/336302650270","taxent/337052971934","taxent/337094980510"
                        ,"taxent/337253381022","taxent/338349994910","taxent/336382473118","taxent/336687936414"
                        ,"taxent/338109608862","taxent/337843991454","taxent/334667002782","taxent/334671721374"
                        ,"taxent/334683255710","taxent/339628667806","taxent/338193298334","taxent/341236462494"
                        ,"taxent/338443383710","taxent/337565528990","taxent/335377740702", "taxent/1489766753625"
                        ,"taxent/335452386206","taxent/341505225630", "taxent/334285452190");
        }

        return Collections.emptyList();
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.redlist_settings;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }
}
