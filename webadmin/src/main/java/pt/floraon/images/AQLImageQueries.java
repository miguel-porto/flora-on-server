package pt.floraon.images;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AQLImageQueries {
    private static final String BUNDLE_NAME = "pt.floraon.images.aqlqueries";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private static final Pattern substitutionPattern = Pattern.compile("\\{@(\\w+)\\}");

    private AQLImageQueries() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
