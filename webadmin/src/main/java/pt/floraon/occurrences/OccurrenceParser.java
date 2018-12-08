package pt.floraon.occurrences;

import org.jfree.util.Log;
import pt.floraon.driver.parsers.CSVParser;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.fields.parsers.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by miguel on 26-03-2017.
 */
public class OccurrenceParser implements CSVParser {
    private IFloraOn driver;
    /**
     * Holds the aliases mappings
     */
    private Map<String, FieldParser> fieldMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, FieldParser> fieldMappingsSecondRound = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private Map<String, FieldParser> matchedParsersFirstRound;
    private Map<String, FieldParser> matchedParsersSecondRound;

    /**
     * Holds a map os user names to DB IDs. This will be updated as needed.
     */
    Map<String, String> userMap = new HashMap<>();

    public OccurrenceParser(IFloraOn driver) {
        this.driver = driver;
/*
        fieldMappings.put("latitude", new LatitudeLongitudeParser());
        fieldMappings.put("longitude", new LatitudeLongitudeParser());
*/
        fieldMappings.put("coordinates", new LatitudeLongitudeParser());
        fieldMappings.put("wkt_geom", new LatitudeLongitudeParser());
        fieldMappings.put("x", new UTMCoordinateParser());
        fieldMappings.put("y", new UTMCoordinateParser());
        fieldMappings.put("mgrs", new UTMCoordinateParser());
        fieldMappings.put("elevation", new IntegerParser());
        fieldMappings.put("taxa", new TaxaParser());
        fieldMappings.put("year", new IntegerParser());
        fieldMappings.put("month", new IntegerParser());
        fieldMappings.put("day", new IntegerParser());
        fieldMappings.put("date", new DateParser());
        fieldMappings.put("precision", new LocalityParser());
        fieldMappings.put("code", new PlainTextParser());
//        fieldMappings.put("locality", new LocalityParser());
//        fieldMappings.put("municipality", new LocalityParser());
//        fieldMappings.put("province", new LocalityParser());
//        fieldMappings.put("county", new LocalityParser());
        fieldMappings.put("verblocality", new LocalityParser());
//        fieldMappings.put("habitat", new PlainTextParser());
//        fieldMappings.put("threats", new PlainTextParser());
//        fieldMappings.put("pubNotes", new PlainTextParser());
//        fieldMappings.put("privNotes", new PlainTextParser());

        // aliases
        fieldMappings.put("observers", new UserListParser(userMap, driver, false));
        fieldMappings.put("collectors", new UserListParser(userMap, driver, false));
        fieldMappings.put("dets", new UserListParser(userMap, driver, false));
        fieldMappings.put("ano", new AliasFieldParser("year", fieldMappings));
        fieldMappings.put("código", new AliasFieldParser("code", fieldMappings));
        fieldMappings.put("inventário", new AliasFieldParser("code", fieldMappings));
        fieldMappings.put("data", new AliasFieldParser("date", fieldMappings));
        fieldMappings.put("time", new AliasFieldParser("date", fieldMappings));
        fieldMappings.put("name", new AliasFieldParser("gpsCode", fieldMappings));
        fieldMappings.put("z", new AliasFieldParser("elevation", fieldMappings));
        fieldMappings.put("região", new AliasFieldParser("province", fieldMappings));
        fieldMappings.put("concelho", new AliasFieldParser("municipality", fieldMappings));
        fieldMappings.put("altitude", new AliasFieldParser("elevation", fieldMappings));
        fieldMappings.put("determiners", new AliasFieldParser("dets", fieldMappings));
        fieldMappings.put("observer", new AliasFieldParser("observers", fieldMappings));
        fieldMappings.put("inventoryid", new PlainTextParser());


//        fieldMappingsSecondRound.put("gpscode", new PlainTextParser());
//        fieldMappingsSecondRound.put("comment", new PlainTextParser());
//        fieldMappingsSecondRound.put("privateComment", new PlainTextParser());
//        fieldMappingsSecondRound.put("labeldata", new PlainTextParser());
//        fieldMappingsSecondRound.put("specificthreats", new PlainTextParser());
//        fieldMappingsSecondRound.put("accession", new PlainTextParser());
//        fieldMappingsSecondRound.put("coverIndex", new PlainTextParser());
        fieldMappingsSecondRound.put("abundance", new NumericIntervalParser());
        fieldMappingsSecondRound.put("typeofestimate", new EnumParser());
        fieldMappingsSecondRound.put("phenostate", new EnumParser());
        fieldMappingsSecondRound.put("confidence", new EnumParser());
        fieldMappingsSecondRound.put("presencestatus", new EnumParser());
        fieldMappingsSecondRound.put("hasphoto", new EnumParser());
        fieldMappingsSecondRound.put("hasspecimen", new IntegerParser());
        fieldMappingsSecondRound.put("occurrenceuuid", new UUIDParser());
/*
        fieldMappingsSecondRound.put("observationlatitude", new LatitudeLongitudeParser());
        fieldMappingsSecondRound.put("observationlongitude", new LatitudeLongitudeParser());
*/
        fieldMappingsSecondRound.put("observationcoordinates", new LatitudeLongitudeParser());
        fieldMappingsSecondRound.put("codHerbario", new AliasFieldParser("accession", fieldMappingsSecondRound));
        fieldMappingsSecondRound.put("excludeReason", new AliasFieldParser("presencestatus", fieldMappingsSecondRound));
        fieldMappingsSecondRound.put("privatenote", new AliasFieldParser("privateComment", fieldMappingsSecondRound));

        fieldMappingsSecondRound.put("gps", new AliasFieldParser("gpsCode", fieldMappingsSecondRound));
        fieldMappingsSecondRound.put("gps code", new AliasFieldParser("gpsCode", fieldMappingsSecondRound));

        // Add field parsers for the inventory fields, from the annotations.
        for(Field field : Inventory.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(pt.floraon.driver.annotations.FieldParser.class)) {
                try {
                    fieldMappings.put(field.getName()
                            , field.getAnnotation(pt.floraon.driver.annotations.FieldParser.class).value().newInstance());
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }

        // Add field parsers for the observation fields, from the annotations.
        for(Field field : OBSERVED_IN.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(pt.floraon.driver.annotations.FieldParser.class)) {
                try {
                    fieldMappingsSecondRound.put(field.getName()
                            , field.getAnnotation(pt.floraon.driver.annotations.FieldParser.class).value().newInstance());
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void registerParser(String fieldName, FieldParser parser) {
        fieldMappings.put(fieldName, parser);
    }

    public Map<String, String> getUserMap() {
        return this.userMap;
    }

    /**
     * Checks if all passed names are recognized.
     * @param names
     * @throws FloraOnException If any name is not recognized.
     */
    public void checkFieldNames(Set<String> names) throws FloraOnException {
        List<String> errors = new ArrayList<>();
        matchedParsersFirstRound = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        matchedParsersSecondRound = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for(String name : names) {
            name = name.replaceAll("\t", "").trim();
            if(name.length() == 0) continue;

            if (!fieldMappings.containsKey(name.toLowerCase()) && !fieldMappingsSecondRound.containsKey(name.toLowerCase())) {
                errors.add(Messages.getString("error.1", name));
                continue;
            }

            for(String s : fieldMappings.keySet()) {
                if(s.equalsIgnoreCase(name)) {
                    matchedParsersFirstRound.put(s, fieldMappings.get(s));
                }
            }

            for(String s : fieldMappingsSecondRound.keySet()) {
                if(s.equalsIgnoreCase(name)) {
                    matchedParsersSecondRound.put(s, fieldMappingsSecondRound.get(s));
                }
            }
        }

        if(errors.size() > 0)
            throw new FloraOnException(StringUtils.implode("\n", errors.toArray(new String[errors.size()])));
    }

    @Override
    public void parseFields(Map<String, String> keyValues, Object bean) throws FloraOnException {
        List<String> errors = new ArrayList<>();
        Inventory inv = (Inventory) bean;
        if(matchedParsersFirstRound != null && matchedParsersSecondRound != null) {
            // first round
            for (Map.Entry<String, FieldParser> entry : matchedParsersFirstRound.entrySet()) {
                entry.getValue().parseValue(keyValues.get(entry.getKey().toLowerCase()), entry.getKey(), inv);
            }

            for (Map.Entry<String, FieldParser> entry : matchedParsersSecondRound.entrySet()) {
                entry.getValue().parseValue(keyValues.get(entry.getKey().toLowerCase()), entry.getKey(), inv);
            }
        } else {    // this is a fallback to the old code, which requires strict field names
            Log.info("Falling back to the old occurrence parser, this requires strict field names");
            // first round
            Set<String> intersection = new HashSet<>(keyValues.keySet());
            intersection.retainAll(fieldMappings.keySet());
            for(String key : intersection) {
                key = key.replaceAll("\t", "");
                if (!fieldMappings.containsKey(key.toLowerCase())) continue;
                try {
                    String tmp = keyValues.get(key);
                    if(tmp != null) tmp = tmp.trim();
//                if(tmp != null) System.out.println("Col: "+key.toLowerCase()+" value: "+Hex.encodeHexString(tmp.getBytes()));
                    fieldMappings.get(key.toLowerCase()).parseValue(tmp, key, inv);
                } catch(IllegalArgumentException | FloraOnException e) {
                    errors.add(e.getMessage());
                }
            }

            // second round
            intersection = new HashSet<>(keyValues.keySet());
            intersection.retainAll(fieldMappingsSecondRound.keySet());
            for(String key : intersection) {
                key = key.replaceAll("\t", "");
                if (!fieldMappingsSecondRound.containsKey(key.toLowerCase())) continue;
                try {
                    String tmp = keyValues.get(key);
                    if(tmp != null) tmp = tmp.trim();
//                if(tmp != null) System.out.println("Col: "+key.toLowerCase()+" key: "+key);
                    fieldMappingsSecondRound.get(key.toLowerCase()).parseValue(tmp, key, inv);
                } catch(IllegalArgumentException | FloraOnException e) {
                    errors.add(e.getMessage());
                }
            }
            if(errors.size() > 0)
                throw new FloraOnException(StringUtils.implode("; ", errors.toArray(new String[errors.size()])));
        }

/*
        for(String s : keyValues.keySet()) {
            if(fieldMappingsSecondRound.containsKey(s)) {
            }
        }

        System.out.println("GIVEN:");
        for(String a : keyValues.keySet())
            System.out.println(a);

        System.out.println("RETAINED:");
        for(String a : intersection)
            System.out.println(a);
*/

    }
}