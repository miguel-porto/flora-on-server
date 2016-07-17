package pt.floraon.arangodriver;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class AQLQueries {
	private static final String BUNDLE_NAME = "pt.floraon.arangodriver.aqlqueries"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private AQLQueries() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String getString(String key, Object... params  ) {
        try {
            return String.format(RESOURCE_BUNDLE.getString(key), params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
