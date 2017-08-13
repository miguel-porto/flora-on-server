package pt.floraon.driver.utils;

import jline.internal.Log;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.entities.NamedDBNode;
import pt.floraon.geocoding.entities.MatchedToponym;
import pt.floraon.occurrences.Common;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 14-02-2017.
 */
public class StringUtils {
    private static final Whitelist whiteTagList = Whitelist.none();
    static {
        whiteTagList.addTags("br", "span", "i", "sup")
                .addAttributes("span", "data-id")
                .addAttributes("span", "contenteditable")
                .addAttributes("span", "class");
    }

    public static Collection<String> cleanCollection(Collection<String> tmp, boolean returnEmpty) {
        tmp.removeAll(Collections.singleton(""));
        tmp.removeAll(Collections.<String> singleton(null));
        if(!returnEmpty && tmp.size() == 0) tmp.add(null);
        return tmp;
    }

    public static String[] cleanArray(String[] array, boolean returnEmpty) {
        List<String> tmp = new ArrayList<>(Arrays.asList(array));
        tmp.removeAll(Collections.singleton(""));
        tmp.removeAll(Collections.<String> singleton(null));
        if(!returnEmpty && tmp.size() == 0) tmp.add(null);
        return tmp.toArray(new String[tmp.size()]);
    }

    /**
     * Cleans an array from the empty strings, but never returns an empty array. This is needed for the javabeans,
     * where we must distinguish an empty array from a null value.
     * @param array
     * @return
     */
    public static String[] cleanArray(String[] array) {
        return cleanArray(array, false);
    }

    /**
     * Fully clean text from HTML tags and &nbsp
     * @param text
     * @return
     */
    public static String cleanText(String text) {
        if(text == null) return "";
        return Jsoup.parse(text).text().replace("\u00a0", " ").trim();
    }

    /**
     * Keep only tags and attributes used in basic formatting
     * @param text
     * @return
     */
    public static String safeHtmlTagsOnly(String text) {
        return Jsoup.clean(text, whiteTagList);
    }

    public static String sanitizeHtmlId(String txt) {
        return txt.replaceAll(Constants.sanitizeHtmlId, "");
    }

    public static <T extends GeneralDBNode> List<String> getIDsList(List<T> nodeList) {
		Iterator<T> it = nodeList.iterator();
		List<String> out = new ArrayList<String>();
		while(it.hasNext()) {
			out.add(it.next().getID());
		}
		return out;
    }

    /**
     * Gets a Set with the names of the given nodes
     * @param nodeList
     * @return
     */
    public static <T extends NamedDBNode> Set<String> getNamesSet(Set<T> nodeList) {
		Iterator<T> it = nodeList.iterator();
		Set<String> out = new HashSet<String>();
		while(it.hasNext()) {
			out.add(it.next().getName());
		}
		return out;
    }

    public static String implode(String separator, String... data) {
        StringBuilder sb = new StringBuilder();
        boolean any = false;
        for (int i = 0; i < data.length; i++) {
            if(data[i] != null && !data[i].matches(" *")) {
                sb.append(data[i]);
                any = true;
                if(i < data.length-1 && data[i+1] != null && !data[i+1].matches(" *")) sb.append(separator);
            }
        }
    	return any ? sb.toString() : null;
    }

    @SafeVarargs
	public static <E extends Enum<E>> String implode(String separator, E... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            if (!data[i].toString().matches(" *")) {
                sb.append(data[i]);
                sb.append(separator);
            }
        }
        sb.append(data[data.length - 1].toString().trim());
        return sb.toString();
    }

    public static boolean isArrayEmpty(Object[] array) {
        return array == null || array.length == 0 || (array.length == 1 && array[0] == null);
    }

    public static <T extends Enum<T>> T[] stringArrayToEnumArray(String[] stringArray, Class<T> clazz) {
        List<T> tmp = new ArrayList<>();
        boolean addNull = false;
        for(String s : stringArray) {
            if(s == null || s.length() == 0) {
                addNull = true;
                continue;
            }
            try {
                tmp.add(T.valueOf(clazz, s));
            } catch (IllegalArgumentException e) {
                Log.warn("Enum value " + s + " not found in " + clazz.toString());
            }
        }

        // this is to distinguish between a null value and an empty array
        if(tmp.size() == 0 && addNull) tmp.add(null);
        return tmp.toArray((T[]) Array.newInstance(clazz, tmp.size()));
    }

    public static Integer getMaxOfInterval(String interval) {
        // FIXME this must process numeric ranges
        Pattern intervalMatch = Pattern.compile("^([0-9]+) *- *([0-9]+)$");
        Matcher mat = intervalMatch.matcher(interval);
        try {
            if (mat.find()) {
                return Integer.valueOf(mat.group(2));
            } else {
                return Integer.valueOf(interval);
            }
        } catch (NumberFormatException e) {
//            e.printStackTrace();
            return null;
        }
    }

    public String randomString(int n) {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        return uuid.substring(uuid.length() - n);
    }

    public static int fuzzyWordMatch(String phrase, String query) {
//        query = query.trim().toLowerCase();
//        phrase = phrase.trim().toLowerCase();
        if(phrase.length() < 3) return 1000;
        if(phrase.equals(query)) return 0;
        if(phrase.contains(query)) return 1;
        int d = 1000;

        boolean containsSpace = phrase.contains(" ");
        boolean containsHifen = phrase.contains("-");

        if(query.contains(" ") || query.contains("-") || (!containsSpace && !containsHifen)) {
            // multiple word query, so fall back to less intelligent match (for now...)
            String first = query.substring(0, 3);

            if(Math.abs(phrase.length() - query.length()) < 3) {
                d = Common.levenshteinDistance(phrase, query) - (phrase.startsWith(first) ? 1 : 0); // give more weight to the first letters
//                if(d<3) System.out.println(phrase+", "+query+": "+d);
            }

            return d;
        }

        String[] words;
        if(containsSpace)
            words = phrase.split(" ");
        else
            words = phrase.split("-");

        for(String word : words) {
            d = fuzzyWordMatch(word, query) + 1;    // give more weight to the whole match
            if(d < 4) break;
        }

        return d;
    }

}
