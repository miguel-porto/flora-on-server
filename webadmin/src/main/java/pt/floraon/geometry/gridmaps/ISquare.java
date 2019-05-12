package pt.floraon.geometry.gridmaps;

import java.awt.geom.Rectangle2D;

/**
 * Represents a square used to build graduated maps
 */
public interface ISquare {
    String getMGRS();
    Rectangle2D getSquare();
    boolean hasColor();
    String getColor();
    void setColor(String color);
}
