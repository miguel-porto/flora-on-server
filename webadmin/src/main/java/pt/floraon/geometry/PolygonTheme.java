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

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads FeatureCollection of polygons from a GeoJson stream, but currently only supports single part polygons without holes!
 * Created by miguel on 02-12-2016.
 */
public class PolygonTheme implements IPolygonTheme {
    private Multimap<String, pt.floraon.geometry.Polygon> polygons;
    private static Pattern wktParse = Pattern.compile("polygon *\\( *\\( *[0-9., -]+ *\\) *\\)", Pattern.CASE_INSENSITIVE);
    private static Pattern wktCoordinate = Pattern.compile("(?<lng>[0-9-]+(\\.[0-9]+)?) +(?<lat>[0-9-]+(\\.[0-9]+)?) *[,)]", Pattern.CASE_INSENSITIVE);
    private float minX, maxX, minY, maxY;

    public PolygonTheme(String WKTString) {
        Matcher mat = wktParse.matcher(WKTString);
        if(mat.find()) {
            pt.floraon.geometry.Polygon pol = new pt.floraon.geometry.Polygon();
            mat = wktCoordinate.matcher(WKTString);
            while(mat.find()) {
                Float lng, lat;
                lng = Float.parseFloat(mat.group("lng"));
                lat = Float.parseFloat(mat.group("lat"));
                pol.add(new Point2D(lng, lat));
                if(lat > maxY) maxY = lat;
                if(lat < minY) minY = lat;
                if(lng > maxX) maxX = lng;
                if(lng < minX) minX = lng;
            }
            this.polygons = ArrayListMultimap.create();
            this.polygons.put("1", pol);
        } else
            throw new IllegalArgumentException("Invalid WKT polygon. The format is POLYGON((longitude latitude, longitude latitude, ...))");
    }

    public Float[] getBoundingBox() {
        return new Float[] {minX, maxX, minY, maxY};
    }
    /**
     * Create from geoJson and construct a map.
     * @param geoJsonStream
     * @param keyFieldName The name of the field for use as the key. Key needs not be unique. If null, key will be the serial number.
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
/*
                    if(ll.getLatitude() > maxY) maxY = (float) ll.getLatitude();
                    if(ll.getLatitude() < minY) minY = (float) ll.getLatitude();
                    if(ll.getLongitude() > maxX) maxX = (float) ll.getLongitude();
                    if(ll.getLongitude() < minX) minX = (float) ll.getLongitude();
*/
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
