package pt.floraon.images;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.DocumentCreateOptions;
import pt.floraon.driver.BaseFloraOnDriver;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.entities.Image;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.IImageManagement;

public class ImageManagementArangoDriver extends BaseFloraOnDriver implements IImageManagement {
    private ArangoDatabase database;

    public ImageManagementArangoDriver(IFloraOn driver) {
        super(driver);
        this.database = (ArangoDatabase) driver.getDatabase();
    }

    @Override
    public Image addNewImage(Image image) throws DatabaseException {
        try {
            return database.collection(image.getTypeAsString()).insertDocument(image, new DocumentCreateOptions().returnNew(true)).getNew();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }

    }
}
