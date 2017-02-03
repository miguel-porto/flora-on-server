package pt.floraon.redlistdata;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Created by miguel on 02-02-2017.
 */
public class FieldValues {
    private static final String BUNDLE_NAME = "pt.floraon.redlistdata.fieldValues";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private FieldValues() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}