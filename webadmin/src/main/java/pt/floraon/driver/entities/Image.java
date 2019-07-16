package pt.floraon.driver.entities;

import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.datatypes.Rectangle;
import pt.floraon.driver.utils.StringUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * The data type for images. Represents a "pointer" to an image file on disk.
 */
public class Image extends GeneralDBNode implements Serializable {
	private String fileName;
	private String uuid;
    private Integer width, height;
	private Rectangle crop;
	private String author, comment;

    public Image() {
    }

    public Image(String fileName, String uuid) {
        if(uuid.length() < 4) throw new IllegalArgumentException("uuid too short");
        this.fileName = fileName;
        this.uuid = uuid;
    }

    static public Image createNew() {
    	Image out = new Image();
    	out.uuid = StringUtils.randomString(4);
    	return out;
	}

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
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
