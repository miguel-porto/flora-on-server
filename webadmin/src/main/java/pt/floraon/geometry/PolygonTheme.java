package pt.floraon.geometry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jline.internal.Log;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Reads FeatureCollection of polygons from a GeoJson stream, but currently only supports single part polygons without holes!
 * Created by miguel on 02-12-2016.
 */
public class PolygonTheme implements IPolygonTheme {
    private Multimap<String, pt.floraon.geometry.Polygon> polygons;

    /**
     * Create from geoJson and construct a map.
     * @param geoJsonStream
     * @param keyFieldName The name of the field for use as the key. Key needs not be unique.
     */
    public PolygonTheme(InputStream geoJsonStream, String keyFieldName) {
        FeatureCollection features;
        try {
            features = new ObjectMapper().readValue(geoJsonStream, FeatureCollection.class);
            geoJsonStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        int npoly = 0;

        // Multimap to allow multiple polygons with the same key
        this.polygons = ArrayListMultimap.create();

        String key;
        for(Feature f : features) {
            if(Polygon.class.isInstance(f.getGeometry())) {
                pt.floraon.geometry.Polygon pol = new pt.floraon.geometry.Polygon();
                List<LngLatAlt> tmp2 = ((Polygon) f.getGeometry()).getExteriorRing();
                LngLatAlt ll;
                for (int i = 0; i < tmp2.size() - 1; i++) {
                    ll = tmp2.get(i);
                    pol.add(new Point2D(ll.getLongitude(), ll.getLatitude()));
                }

                pol.setProperties(f.getProperties());

                if(keyFieldName != null)
                    key = f.getProperties().get(keyFieldName).toString();
                else
                    key = "" + npoly;
                this.polygons.put(key, pol);
                npoly++;
            } else Log.warn("Feature of type "+f.getGeometry().getClass()+" ignored.");
        }

    }

    @Override
    public Iterator<Map.Entry<String, pt.floraon.geometry.Polygon>> iterator() {
        if(polygons == null)
            return Collections.emptyIterator();
        else
            return polygons.entries().iterator();
    }

    @Override
    public Collection<pt.floraon.geometry.Polygon> get(String k) {
        if(polygons == null)
            return Collections.emptyList();
        else
            return this.polygons.get(k);
    }

    @Override
    public int size() {
        return polygons == null ? 0 : polygons.size();
    }
}
