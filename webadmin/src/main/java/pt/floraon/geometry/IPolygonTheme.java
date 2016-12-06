package pt.floraon.geometry;

import pt.floraon.geometry.Polygon;

import java.util.Collection;
import java.util.Map;

/**
 * Created by miguel on 02-12-2016.
 */
public interface IPolygonTheme extends Iterable<Map.Entry<String, Polygon>> {
    Collection<Polygon> get(String k);
}
