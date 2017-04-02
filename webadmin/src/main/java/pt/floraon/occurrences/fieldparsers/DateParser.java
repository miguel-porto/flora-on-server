package pt.floraon.occurrences.fieldparsers;

import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a special date parser which accepts null values in any of the three parts.
 * Created by miguel on 29-03-2017.
 */
public class DateParser implements FieldParser {
    static final private Pattern datePattern =
            Pattern.compile("^ *(?<day>[0-9?-]{1,2})(?:-|/)(?<month>[0-9?-]{1,2})(?:-|/)(?<year>([0-9]{4})|([-?]{1,4})) *$");

    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("")) return;
        Integer day, month, year;
        Matcher matcher = datePattern.matcher(inputValue);
        if(!matcher.find()) throw new IllegalArgumentException("Date format not recognized.");

        try {
            day = Integer.parseInt(matcher.group("day"));
        } catch (NumberFormatException e) {
            day = null;
        }

        try {
            month = Integer.parseInt(matcher.group("month"));
        } catch (NumberFormatException e) {
            month = null;
        }

        try {
            year = Integer.parseInt(matcher.group("year"));
        } catch (NumberFormatException e) {
            year = null;
        }

        if(day != null && month != null && year != null) {
            Calendar c = new GregorianCalendar();
            c.setLenient(false);
            c.set(Calendar.DAY_OF_MONTH, day);
            c.set(Calendar.MONTH, month - 1);
            c.set(Calendar.YEAR, year);

            try {
                c.getTime();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date: " + inputValue);
            }
        } else {
            if(day != null && (day < 1 || day > 31)) throw new IllegalArgumentException("Invalid day: " + day);
            if(month != null && (month < 1 || month > 12)) throw new IllegalArgumentException("Invalid month: " + month);
        }

        occurrence.setDay(day);
        occurrence.setMonth(month);
        occurrence.setYear(year);
    }
}
