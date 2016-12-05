package pt.floraon.redlistdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jline.internal.Log;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import pt.floraon.geometry.Point2D;

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
    private Multimap<String, pt.floraon.geometry.Polygon> protectedAreas;

    /**
     * Create from geoJson and construct a map.
     * @param geoJsonStream
     * @param fieldName
     */
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
        this.protectedAreas = ArrayListMultimap.create();

        String name;
        for(Feature f : protectedAreas) {
            if(Polygon.class.isInstance(f.getGeometry())) {
                pt.floraon.geometry.Polygon pol = new pt.floraon.geometry.Polygon();
                List<LngLatAlt> tmp2 = ((Polygon) f.getGeometry()).getExteriorRing();
                LngLatAlt ll;
                for (int i = 0; i < tmp2.size() - 1; i++) {
                    ll = tmp2.get(i);
                    pol.add(new Point2D(ll.getLongitude(), ll.getLatitude()));
                }
                if(fieldName != null) {
                    name = f.getProperties().get(fieldName).toString();
                } else name = "" + npoly;
                this.protectedAreas.put(name, pol);
                npoly++;
            } else Log.warn("Feature of type "+f.getGeometry().getClass()+" ignored.");
        }

    }

    @Override
    public Iterator<Map.Entry<String, pt.floraon.geometry.Polygon>> iterator() {
        return protectedAreas.entries().iterator();
    }
}
