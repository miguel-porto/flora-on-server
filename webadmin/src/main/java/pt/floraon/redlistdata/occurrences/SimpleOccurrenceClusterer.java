package pt.floraon.redlistdata.occurrences;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Point2DAlwaysDifferent;
import pt.floraon.geometry.UTMCoordinate;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrence;

import java.util.*;

/**
 * Created by miguel on 02-07-2017.
 */
public class SimpleOccurrenceClusterer {
    private final Multimap<Integer, SimpleOccurrence> out;

    public SimpleOccurrenceClusterer(Iterator<SimpleOccurrence> simpleOccurrences, int minDist) {
        Map<Point2D, SimpleOccurrence> occMap = new HashMap<>();
        out = LinkedListMultimap.create();
        Multimap<Integer, SimpleOccurrence> singletons = LinkedListMultimap.create();
        int counter = 0;

        while(simpleOccurrences.hasNext()) {
            SimpleOccurrence so = simpleOccurrences.next();
            UTMCoordinate utm = so._getUTMCoordinates();
            if(utm != null) {
                if(!so.getPrecision()._isImprecise())
                    occMap.put(new Point2DAlwaysDifferent(utm.getX(), utm.getY(), null, null), so);
                else {
                    singletons.put(counter, so);
                    counter++;
                }
            }
        }
        DBSCANClusterer<Point2D> cls = new DBSCANClusterer<>(minDist, 0);
        List<Cluster<Point2D>> clusters = cls.cluster(occMap.keySet());

        for (int i = 0; i < clusters.size(); i++) {
            for(Point2D p : clusters.get(i).getPoints()) {
                out.put(i + counter, occMap.get(p));
            }
        }
        out.putAll(singletons);
    }

    public Multimap<Integer, SimpleOccurrence> getClusteredOccurrences() {
        return this.out;
    }
}
