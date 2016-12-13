package pt.floraon.arangodriver;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import pt.floraon.driver.*;
import pt.floraon.authentication.PasswordAuthentication;
import pt.floraon.authentication.entities.User;

import java.util.List;

/**
 * Created by miguel on 26-11-2016.
 */
public class Administration extends BaseFloraOnDriver implements IAdministration {
    private ArangoDatabase database;

    public Administration(IFloraOn driver) {
        super(driver);
        this.database = (ArangoDatabase) driver.getDatabase();
    }

    @Override
    public List<User> getAllUsers() throws FloraOnException {
        String query = AQLQueries.getString("Administration.1");
        try {
            return database.query(query, null, null, User.class).asListRemaining();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    @Override
    public INodeKey createUser(User user) throws FloraOnException {
        try {
            user = database.collection(Constants.NodeTypes.user.toString()).insertDocument(user, new DocumentCreateOptions().returnNew(true)).getNew();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
        return driver.asNodeKey(user.getID());
    }

    @Override
    public User getUser(INodeKey id) throws FloraOnException {
        if(id == null) return null;
        try {
            return database.collection(Constants.NodeTypes.user.toString()).getDocument(id.getDBKey(), User.class);
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    @Override
    public User updateUser(INodeKey id, User user) throws FloraOnException {
        try {
            return database.collection(Constants.NodeTypes.user.toString()).updateDocument(id.getDBKey(), user
                    , new DocumentUpdateOptions().serializeNull(false).returnNew(true)).getNew();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    @Override
    public User authenticateUser(String username, char[] password) throws FloraOnException {
        String query = AQLQueries.getString("Administration.2", username);
        PasswordAuthentication pa = new PasswordAuthentication();
        try {
            ArangoCursor<User> cout = database.query(query, null, null, User.class);
            if(cout == null || !cout.hasNext()) return null;
            User out = cout.next();

            if(pa.authenticate(password, out.getPassword()))
                return out;
            else
                return null;
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }
}
