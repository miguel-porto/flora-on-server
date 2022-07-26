package pt.floraon.arangodriver;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.google.gson.Gson;
import jline.internal.Log;
import pt.floraon.authentication.RandomString;
import pt.floraon.driver.*;
import pt.floraon.authentication.PasswordAuthentication;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.interfaces.IAdministration;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.fields.flavours.CustomOccurrenceFlavour;

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
        if(StringUtils.isStringEmpty(user.getUserName()))
            user.setUserName("user_" + new RandomString(8).nextString());
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
    public User getUserByName(String name) throws FloraOnException {
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
    public User getUserByINaturalistName(String iNaturalistName) throws FloraOnException {
        if(iNaturalistName == null) return null;
        ArangoCursor<User> cur;
        Map<String, Object> bind = new HashMap<>();
        bind.put("iNaturalistName", iNaturalistName);
        try {
            cur = database.query(AQLQueries.getString("Administration.4b"), bind
                    , new AqlQueryOptions().count(true), User.class);
            if(!cur.hasNext()) return null;
            if(cur.getCount() > 1) throw new FloraOnException(Messages.getString("error.1", iNaturalistName));
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
        if(StringUtils.isStringEmpty(user.getUserName()))
            user.setUserName("user_" + new RandomString(8).nextString());
        Log.info(new Gson().toJson(user));
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

    public User removeSavedFilter(INodeKey id, String filterName) throws FloraOnException {
        Map<String, Object> bind = new HashMap<>();
        bind.put("uid", id.toString());
        bind.put("filtername", filterName);
        try {
            return database.query(AQLQueries.getString("Administration.10"), bind
                    , null, User.class).next();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    @Override
    public User createCustomOccurrenceFlavour(INodeKey id, String[] fields, String name, boolean showInOccurrenceView
            , boolean showInInventoryView) throws FloraOnException {
        Map<String, Object> bind = new HashMap<>();
        bind.put("user", id.toString());
        bind.put("flavour", new CustomOccurrenceFlavour(fields, name, showInOccurrenceView, showInInventoryView));
        try {
            return database.query(AQLQueries.getString("Administration.6"), bind, null, User.class).next();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    @Override
    public User deleteCustomOccurrenceFlavour(INodeKey id, String name) throws FloraOnException {
        Map<String, Object> bind = new HashMap<>();
        bind.put("user", id.toString());
        bind.put("flavour", name);
        try {
            return database.query(AQLQueries.getString("Administration.7"), bind, null, User.class).next();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    @Override
    public User changeCustomOccurrenceFlavourFieldOrder(INodeKey userId, String flavourName, int index, boolean decrease) throws FloraOnException {
        if(decrease && index < 1) return null;
        Map<String, Object> bind = new HashMap<>();
        bind.put("user", userId.toString());
        bind.put("flavour", flavourName);
        bind.put("ind", decrease ? index : (index + 1));
        try {
            return database.query(AQLQueries.getString("Administration.8"), bind, null, User.class).next();
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

    @Override
    public int getNumberOfOccurrencesOfUser(INodeKey userId) throws FloraOnException {
        Map<String, Object> bind = new HashMap<>();
        bind.put("user", userId.toString());
        try {
            return database.query(AQLQueries.getString("Administration.9"), bind, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new FloraOnException(e.getMessage());
        }
    }
}
