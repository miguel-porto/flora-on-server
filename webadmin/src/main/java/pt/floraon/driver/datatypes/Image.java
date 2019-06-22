package pt.floraon.driver.datatypes;

import java.io.Serializable;

/**
 * The data type for images. Represents a "pointer" to an image file on disk.
 */
public class Image implements Serializable {
    private final String fileName;

    public Image(String fileName) {
        this.fileName = fileName;
    }
}
