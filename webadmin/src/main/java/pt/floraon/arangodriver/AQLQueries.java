package pt.floraon.arangodriver;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class AQLQueries {
	private static final String BUNDLE_NAME = "pt.floraon.arangodriver.aqlqueries";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private static final Pattern substitutionPattern = Pattern.compile("\\{@(\\w+)\\}");

	public AQLQueries() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	@Deprecated
	public static String getString(String key, boolean fragments) {
		String msg = RESOURCE_BUNDLE.getString(key);
		if(fragments) {
			// replace named AQL fragments
			Matcher mat = substitutionPattern.matcher(msg);
			while (mat.find()) {
				msg = msg.replace("{@" + mat.group(1) + "}", getString(mat.group(1)));
			}
			return msg;
		} else return msg;
	}

	@Deprecated
	public static String getString(String key, Object... params) {
		String msg = RESOURCE_BUNDLE.getString(key);
		// replace named AQL fragments
		Matcher mat = substitutionPattern.matcher(msg);
		while (mat.find()) {
			msg = msg.replace("{@" + mat.group(1) + "}", getString(mat.group(1)));
		}
		// now substitute passed variables
		try {
			msg = String.format(msg, params);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
		return msg;
	}

	@Deprecated
	public static String getString(String key, Map<String,String> params) {
		String msg;
		try {
			msg = RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
		for(Entry<String,String> p : params.entrySet()) {
			msg = msg.replace(p.getKey(), p.getValue());
		}
		return msg;
	}
}
