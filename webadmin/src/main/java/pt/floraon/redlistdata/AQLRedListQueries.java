package pt.floraon.redlistdata;

import pt.floraon.arangodriver.AQLQueries;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AQLRedListQueries {
    private static final String BUNDLE_NAME = "pt.floraon.redlistdata.aqlqueries";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private static final Pattern substitutionPattern = Pattern.compile("\\{@(\\w+)\\}");

    private AQLRedListQueries() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getString(String key, Object... params) {
        String msg = RESOURCE_BUNDLE.getString(key);
        // replace named AQL fragments
        Matcher mat = substitutionPattern.matcher(msg);
        while (mat.find()) {
            msg = msg.replace("{@" + mat.group(1) + "}", getString(mat.group(1), params));
        }
        // now substitute passed variables
        try {
            msg = String.format(msg, params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
        return msg;
    }

    // TODO: named params
    public static String getString(String key, Map<String,String> params) {
        String msg;
        try {
            msg = RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
        for(Map.Entry<String,String> p : params.entrySet()) {
            msg = msg.replace(p.getKey(), p.getValue());
        }
        return msg;
    }
}
