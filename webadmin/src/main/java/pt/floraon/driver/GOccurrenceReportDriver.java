package pt.floraon.driver;

import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.IOccurrenceReportDriver;
import pt.floraon.geometry.CoordinateConversion;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Polygon;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.entities.Inventory;

import java.util.*;

/**
 * Created by miguel on 19-07-2017.
 */
public abstract class GOccurrenceReportDriver extends BaseFloraOnDriver implements IOccurrenceReportDriver {
    public GOccurrenceReportDriver(IFloraOn driver) {
        super(driver);
    }

    @Override
    public Map<String, Integer> getListOfUTMSquaresWithOccurrences(INodeKey userId, Date from, Date to, long sizeOfSquare) throws DatabaseException{
        Map<String, Integer> out = new TreeMap<>();
        Inventory i;
        Iterator<Inventory> it = driver.getOccurrenceDriver().getOccurrencesOfObserverWithinDates(
                userId, from, to, null, null);

        while(it.hasNext()) {
            i = it.next();
            String mgrs = CoordinateConversion.LatLongToMGRS(i._getLatitude(), i._getLongitude(), sizeOfSquare);
            if(out.containsKey(mgrs))
                out.put(mgrs, out.get(mgrs) + 1);
            else
                out.put(mgrs, 1);
        }
        return out;
    }

    @Override
    public Map<String, Integer> getListOfPolygonsWithOccurrences(Iterator<Inventory> it, PolygonTheme polygonTheme) throws DatabaseException {
        if(polygonTheme == null) return Collections.emptyMap();
        Map<String, Integer> out = new TreeMap<>();
        Map<Polygon, Integer> tmp = new HashMap<>();
        Inventory i;

        while(it.hasNext()) {
            i = it.next();
            boolean isContained = false;
            for (Map.Entry<String, pt.floraon.geometry.Polygon> e : polygonTheme) {
                if (e.getValue().contains(new Point2D(i._getLongitude(), i._getLatitude()))) {
                    isContained = true;
                    if (tmp.containsKey(e.getValue()))
                        tmp.put(e.getValue(), tmp.get(e.getValue()) + 1);
                    else
                        tmp.put(e.getValue(), 1);
                }
            }
            if(!isContained) {
                if (out.containsKey("&lt;outside&gt;"))
                    out.put("&lt;outside&gt;", out.get("&lt;outside&gt;") + 1);
                else
                    out.put("&lt;outside&gt;", 1);
            }
        }
        for(Map.Entry<Polygon, Integer> e : tmp.entrySet()) {
            // TODO this MUST be user configuration
            String key = e.getKey().getProperties().get("SITE_NAME") + " (" + e.getKey().getProperties().get("TIPO") + ")";
            if (out.containsKey(key))
                out.put(key, out.get(key) + 1);
            else
                out.put(key, 1);
        }

        return out;
    }
}
