package pt.floraon.occurrences.fieldparsers;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses a comma-separated list of user names (person names, not usernames)
 * Created by miguel on 12-02-2017.
 */
public class UserListParser implements FieldParser {
    private Map<String, String> userMap;
    private IFloraOn driver;

    public UserListParser(Map<String, String> userMap, IFloraOn driver) {
        this.userMap = userMap;
        this.driver = driver;
    }

    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException, FloraOnException {
        if(inputValue == null || inputValue.trim().equals("")) return;

        String[] spl = inputValue.split(",");
        String tmp;
        List<String> userIds = new ArrayList<>();

        for(String username : spl) {
            tmp = username.trim();
            if(userMap.containsKey(tmp))
                userIds.add(userMap.get(tmp));
            else {
                User user = driver.getAdministration().getUser(username.trim());
                if(user == null) throw new FloraOnException(Messages.getString("error.2", username.trim()));
                userMap.put(user.getName().toLowerCase(), user.getID());
                userIds.add(user.getID());
            }
        }

        String[] ids = userIds.toArray(new String[userIds.size()]);
        switch (inputFieldName) {
            case "observers":
                occurrence.getSpeciesList().setObservers(ids);
                break;

            case "collectors":
                occurrence.getSpeciesList().setCollectors(ids);
                break;

            case "determiners":
                occurrence.getSpeciesList().setDets(ids);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
