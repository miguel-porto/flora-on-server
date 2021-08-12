package pt.floraon.driver.interfaces;

import com.sun.istack.NotNull;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;

import java.util.Iterator;
import java.util.List;

/**
 * Created by miguel on 26-11-2016.
 */
public interface IAdministration {
    List<User> getAllUsers(boolean onlyWithPassword) throws FloraOnException;
    INodeKey createUser(User user) throws FloraOnException;
    User getUser(INodeKey id) throws FloraOnException;
    User getUserByName(String name) throws FloraOnException;
    User getUserByINaturalistName(String iNaturalistName) throws FloraOnException;

    Iterator<User> findUserByName(String substr) throws FloraOnException;

    User updateUser(INodeKey id, User user) throws FloraOnException;
    User authenticateUser(String username, char[] password) throws FloraOnException;
    User removeTaxonPrivileges(INodeKey id, int index) throws FloraOnException;
    User removeTaxonFromPrivilegeSet(INodeKey userId, INodeKey taxEntId, int indexOfSet) throws FloraOnException;

    User createCustomOccurrenceFlavour(INodeKey id, String[] fields, String name, boolean showInOccurrenceView
            , boolean showInInventoryView) throws FloraOnException;
    User deleteCustomOccurrenceFlavour(INodeKey id, String name) throws FloraOnException;

    User changeCustomOccurrenceFlavourFieldOrder(INodeKey userId, String flavourName, int index, boolean decrease)
        throws FloraOnException;

    /**
     * @param userId
     * @return The number of occurrences where this user is involved. This searched in the fields observers, collectors
     * , dets and maintainer
     * @throws FloraOnException
     */
    int getNumberOfOccurrencesOfUser(INodeKey userId) throws FloraOnException;
}
