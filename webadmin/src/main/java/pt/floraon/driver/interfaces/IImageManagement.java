package pt.floraon.driver.interfaces;

import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.entities.Image;

public interface IImageManagement {
    Image addNewImage(Image image) throws DatabaseException;
    Image getImageFromUUID(String uuid) throws DatabaseException;
}
