package pt.floraon.geometry.gridmaps;

/**
 * A data object associated with each occurrence square in a grid map.
 */
public interface SquareData {
    int getNumber();
    String getText();
    void add(Object o);
}
