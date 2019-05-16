package pt.floraon.geometry.gridmaps;

import java.io.PrintWriter;

/**
 * For classes that can be exported in WKT format.
 */
public interface WKTExportable {
    String toWKT();
    void toWKT(PrintWriter writer);
}
