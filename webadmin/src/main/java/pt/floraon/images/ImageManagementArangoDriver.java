package pt.floraon.images;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import pt.floraon.arangodriver.AQLQueries;
import pt.floraon.driver.BaseFloraOnDriver;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.entities.Image;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.IImageManagement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

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

    @Override
    public Image getImageFromUUID(String uuid) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("uuid", uuid);
        try {
            Iterator<Image> it = database.query(AQLImageQueries.getString("images.1"), bindVars, null, Image.class);
            return it.hasNext() ? it.next() : null;
        } catch (ArangoDBException | NoSuchElementException e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        }
    }
}
