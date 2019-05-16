package pt.floraon.redlistdata;

import pt.floraon.geometry.gridmaps.GridMap;
import pt.floraon.geometry.gridmaps.ISquare;

public interface GridMapExporter {
/*
    void exportSVG(PrintWriter out, boolean showOccurrences, boolean showConvexhull, boolean showBaseMap
            , boolean standAlone, int border, boolean showShadow, boolean showProtectedAreas, RedListSettings redListSettings);
*/

    /**
     * Iterate over the grid squares, instead of the occurrences.
     * These were computed upon creation of the Processor.
     * @return
     */
//    Iterable<Map.Entry<? extends Square, Set<String>>> squares();

    GridMap<? extends ISquare> squares();
}
