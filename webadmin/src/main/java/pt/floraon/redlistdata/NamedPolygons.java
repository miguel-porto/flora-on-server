package pt.floraon.redlistdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jline.internal.Log;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import pt.floraon.utmlatlong.Point2D;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by miguel on 02-12-2016.
 */
public class NamedPolygons implements PolygonTheme {
//    private Map<String, pt.floraon.utmlatlong.Polygon> protectedAreasPolygon;
    private Multimap<String, pt.floraon.utmlatlong.Polygon> protectedAreasP;

    public NamedPolygons(InputStream geoJsonStream, String fieldName) {
        FeatureCollection protectedAreas = null;
        try {
            protectedAreas =
                    new ObjectMapper().readValue(geoJsonStream, FeatureCollection.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int npoly = 0;
//        this.protectedAreasPolygon = new HashMap<>();
        this.protectedAreasP = ArrayListMultimap.create();

        String name;
        for(Feature f : protectedAreas) {
            if(Polygon.class.isInstance(f.getGeometry())) {
                pt.floraon.utmlatlong.Polygon pol = new pt.floraon.utmlatlong.Polygon();
                List<LngLatAlt> tmp2 = ((Polygon) f.getGeometry()).getExteriorRing();
                LngLatAlt ll;
                for (int i = 0; i < tmp2.size() - 1; i++) {
                    ll = tmp2.get(i);
                    pol.add(new Point2D(ll.getLongitude(), ll.getLatitude()));
                }
                if(fieldName != null) {
                    name = f.getProperties().get(fieldName).toString();
/*
                    int cou = 1;
                    while (this.protectedAreasPolygon.keySet().contains(name)) {
                        name = f.getProperties().get(fieldName).toString() + " (" + cou + ")";
                        cou++;
                    }
*/
                } else name = "" + npoly;
                this.protectedAreasP.put(name, pol);
//                this.protectedAreasPolygon.put(name, pol);
                npoly++;
            } else Log.warn("Feature of type "+f.getGeometry().getClass()+" ignored.");
        }

    }

    @Override
    public Iterator<Map.Entry<String, pt.floraon.utmlatlong.Polygon>> iterator() {
        return protectedAreasP.entries().iterator();
    }
}
