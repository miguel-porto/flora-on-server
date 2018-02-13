package pt.floraon.occurrences.fieldparsers;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import pt.floraon.driver.Constants;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a special date parser which accepts null values in any of the three parts.
 * Created by miguel on 29-03-2017.
 */
public class DateParser implements FieldParser {
    static final private Pattern datePattern =
            Pattern.compile("^ *(?:(?<day>[0-9?-]{1,2})(?:-|/|( +)))?(?:(?<month>(?:[0-9?-]{1,2})|(?:[a-zA-Z?-]+))(?:-|/|( +)))?(?<year>([0-9]{1,4})|([-?]{1,4})) *$");

    static final private Map<String, Integer> months = new HashMap<>();

    static {
        months.put("I", 1);
        months.put("II", 2);
        months.put("III", 3);
        months.put("IV", 4);
        months.put("V", 5);
        months.put("VI", 6);
        months.put("VII", 8);
        months.put("VIII", 9);
        months.put("IX", 9);
        months.put("X", 10);
        months.put("XI", 11);
        months.put("XII", 12);
        months.put("JAN", 1);
        months.put("FEV", 2);
        months.put("MAR", 3);
        months.put("ABR", 4);
        months.put("MAI", 5);
        months.put("JUN", 6);
        months.put("JUL", 7);
        months.put("AGO", 8);
        months.put("SET", 9);
        months.put("OUT", 10);
        months.put("NOV", 11);
        months.put("DEZ", 12);
        months.put("JANEIRO", 1);
        months.put("FEVEREIRO", 2);
        months.put("MARÇO", 3);
        months.put("ABRIL", 4);
        months.put("MAIO", 5);
        months.put("JUNHO", 6);
        months.put("JULHO", 7);
        months.put("AGOSTO", 8);
        months.put("SETEMBRO", 9);
        months.put("OUTUBRO", 10);
        months.put("NOVEMBRO", 11);
        months.put("DEZEMBRO", 12);
    }

    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        if(inputValue == null) return;
        Inventory occurrence = (Inventory) bean;
        Integer[] date;

        if(inputValue.trim().equals("")) {
            date = new Integer[] {Constants.NODATA_INT, Constants.NODATA_INT, Constants.NODATA_INT};
        } else
            date = parseDate(inputValue);

        occurrence.setDay(date[0]);
        occurrence.setMonth(date[1]);
        occurrence.setYear(date[2]);
    }

    static public Integer[] parseDate(String inputValue) {
        if(inputValue == null) return null;
        Integer[] outdate = new Integer[3];
        Integer day = null, month = null, year = null;

//        System.out.println("***** INPUT: "+ inputValue);
        Matcher matcher = datePattern.matcher(inputValue);
        if(!matcher.find()) {
//            System.out.println("NAtty");
            // try natty for loose date specification
            Parser dp = new Parser();

            List<DateGroup> grps = dp.parse(inputValue);
            if(grps.size() == 0)
                throw new IllegalArgumentException("Date " + inputValue + " not recognized by Natty.");

            for(DateGroup grp : grps) {
                Calendar c1 = new GregorianCalendar();
                c1.setTime(grp.getDates().get(0));
                day = c1.get(Calendar.DAY_OF_MONTH);
                month = c1.get(Calendar.MONTH) + 1;
                year = c1.get(Calendar.YEAR);
                System.out.println(Constants.dateFormat.get().format(grp.getDates().get(0)));
            }
        } else {
            String dayS = matcher.group("day");
            String monthS = matcher.group("month");
            if(dayS != null && monthS == null) {
                String tmpS = dayS;
                dayS = monthS;
                monthS = tmpS;
            }

            if(dayS != null) {
                try {
                    day = Integer.parseInt(dayS);
                } catch (NumberFormatException e) {
                    day = null;
                }
            }

            if(monthS != null) {
                if(months.containsKey(monthS.toUpperCase()))
                    month = months.get(monthS.toUpperCase());
                else {
                    try {
                        month = Integer.parseInt(monthS);
                    } catch (NumberFormatException e) {
                        month = null;
                    }
                }
            }

            try {
                year = Integer.parseInt(matcher.group("year"));
                if(matcher.group("year").length() != 4)
                    throw new IllegalArgumentException(Messages.getString("error.13", inputValue));
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
            if(day == null) day = Constants.NODATA_INT;
            if(month == null) month = Constants.NODATA_INT;
            if(year == null) year = Constants.NODATA_INT;
        }
        outdate[0] = day;
        outdate[1] = month;
        outdate[2] = year;
        return outdate;
    }

    static public Date parseDateAsDate(String inputValue) {
        Integer[] date = parseDate(inputValue);
        if(date == null) return null;
        Calendar c = new GregorianCalendar();
        c.setLenient(false);
        c.set(Calendar.DAY_OF_MONTH, date[0]);
        c.set(Calendar.MONTH, date[1] - 1);
        c.set(Calendar.YEAR, date[2]);
        return c.getTime();
    }
}
