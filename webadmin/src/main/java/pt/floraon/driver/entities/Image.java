package pt.floraon.driver.entities;

import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.datatypes.Rectangle;

import java.io.Serializable;
import java.util.UUID;

/**
 * The data type for images. Represents a "pointer" to an image file on disk.
 */
public class Image extends GeneralDBNode implements Serializable {
	private String fileName;
	private UUID uuid;
	private Integer width, height;
	private Rectangle crop;
	private String author, comment;

    public Image() {
    }

    public Image(String fileName, String uuid) {
        this.fileName = fileName;
        this.uuid = UUID.fromString(uuid);
    }

    @Override
    public String toString() {
        return this.fileName;
    }

    @Override
	public String getTypeAsString() {
		return this.getType().toString();
	}
	
	@Override
	public NodeTypes getType() {
		return NodeTypes.image;
	}

}
