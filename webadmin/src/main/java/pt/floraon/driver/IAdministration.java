package pt.floraon.driver;

import pt.floraon.authentication.entities.User;

import java.util.List;

/**
 * Created by miguel on 26-11-2016.
 */
public interface IAdministration {
    List<User> getAllUsers() throws FloraOnException;
    INodeKey createUser(User user) throws FloraOnException;
    User getUser(INodeKey id) throws FloraOnException;
    User getUser(String name) throws FloraOnException;
    User updateUser(INodeKey id, User user) throws FloraOnException;
    User authenticateUser(String username, char[] password) throws FloraOnException;
    User removeTaxonPrivileges(INodeKey id, int index) throws FloraOnException;
}
