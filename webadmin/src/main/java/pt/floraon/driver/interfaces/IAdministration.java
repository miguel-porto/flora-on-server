package pt.floraon.driver.interfaces;

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
    User getUser(String name) throws FloraOnException;

    Iterator<User> findUserByName(String substr) throws FloraOnException;

    User updateUser(INodeKey id, User user) throws FloraOnException;
    User authenticateUser(String username, char[] password) throws FloraOnException;
    User removeTaxonPrivileges(INodeKey id, int index) throws FloraOnException;
    User removeTaxonFromPrivilegeSet(INodeKey userId, INodeKey taxEntId, int indexOfSet) throws FloraOnException;
}
