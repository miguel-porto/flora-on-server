package pt.floraon.occurrences.fieldparsers;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import pt.floraon.driver.Constants;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.entities.Inventory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
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
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("")) return;
        Inventory occurrence = (Inventory) bean;
        Integer day = null, month = null, year = null;

        System.out.println("***** INPUT: "+ inputValue);
        Matcher matcher = datePattern.matcher(inputValue);
        if(!matcher.find()) {
            System.out.println("NAtty");
            // try natty for loose date specification
            Parser dp = new Parser();

            List<DateGroup> grps = dp.parse(inputValue);
            if(grps.size() == 0)
                throw new IllegalArgumentException("Date format not recognized.");

            for(DateGroup grp : grps) {
                Calendar c1 = new GregorianCalendar();
                c1.setTime(grp.getDates().get(0));
                day = c1.get(Calendar.DAY_OF_MONTH);
                month = c1.get(Calendar.MONTH) + 1;
                year = c1.get(Calendar.YEAR);
                System.out.println(Constants.dateFormat.format(grp.getDates().get(0)));
            }
        } else {
            System.out.println("Regex");
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
