package pt.floraon.redlistdata;

import java.io.PrintWriter;

public interface SVGMapExporter {
    void exportSVG(PrintWriter out, boolean showOccurrences, boolean showConvexhull, boolean showBaseMap
            , boolean standAlone, int border, boolean showShadow, boolean showProtectedAreas);
}
