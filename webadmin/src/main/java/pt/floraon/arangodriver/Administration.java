package pt.floraon.arangodriver;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import pt.floraon.driver.*;
import pt.floraon.authentication.PasswordAuthentication;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.interfaces.IAdministration;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.utils.StringUtils;

import java.util.*;

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
    public List<User> getAllUsers(boolean onlyWithPassword) throws FloraOnException {
        String query = AQLQueries.getString(onlyWithPassword ? "Administration.1a" : "Administration.1");
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
    public User getUser(String name) throws FloraOnException {
        if(name == null) return null;
        ArangoCursor<User> cur;
        Map<String, Object> bind = new HashMap<>();
        bind.put("name", name);
        try {
            cur = database.query(AQLQueries.getString("Administration.4"), bind
                    , new AqlQueryOptions().count(true), User.class);
            if(!cur.hasNext()) return null;
            if(cur.getCount() > 1) throw new FloraOnException(Messages.getString("error.1", name));
            return cur.next();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    @Override
    public Iterator<User> findUserByName(String substr) throws FloraOnException {
        if(StringUtils.isStringEmpty(substr))
            return Collections.emptyIterator();
        Map<String, Object> bind = new HashMap<>();
        bind.put("name", substr.replaceAll("\\*", "%"));
        try {
            return database.query(AQLQueries.getString("Administration.4a"), bind
                    , null, User.class);
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
    public User removeTaxonPrivileges(INodeKey id, int index) throws FloraOnException {
        try {
            return database.query(AQLQueries.getString("Administration.3", id.getID(), index), null
                    , null, User.class).next();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    @Override
    public User removeTaxonFromPrivilegeSet(INodeKey userId, INodeKey taxEntId, int indexOfSet) throws FloraOnException {
        try {
            return database.query(AQLQueries.getString("Administration.5", userId.getID(), indexOfSet, taxEntId)
                    , null, null, User.class).next();
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
