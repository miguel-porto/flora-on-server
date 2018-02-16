package pt.floraon.occurrences.fieldparsers;

import com.google.gson.Gson;
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
    static final private Pattern singleDatePattern =
            Pattern.compile("^ *(?:(?<day>[0-9?-]{1,2})(?:-|/|( +)))?(?:(?<month>(?:[0-9?-]{1,2})|(?:[a-zA-Z?-]+))(?:-|/|( +)))?(?<year>([0-9]{1,4})|([-?]{1,4})) *$");
    static final private Pattern dateRangePattern =
            Pattern.compile("^ *(?:(?<day1>[0-9?-]{1,2})(?:-|/|( +)))?(?:(?<month1>(?:[0-9?-]{1,2})|(?:[a-zA-Z?-]+))(?:-|/|( +)))?(?<year1>([0-9]{1,4})|([-?]{1,4})) *" +
                    "- *(?:(?<day2>[0-9?-]{1,2})(?:-|/|( +)))?(?:(?<month2>(?:[0-9?-]{1,2})|(?:[a-zA-Z?-]+))(?:-|/|( +)))?(?<year2>([0-9]{1,4})|([-?]{1,4})) *$");

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

    /**
     * Parses a single date or a date range.
     * @param inputValue A date string.
     * @return A 3 Integer array for a single date, a 6 Integer array for a date range.
     */
    static public Integer[] parseDate(String inputValue) {
        if(inputValue == null) return null;
        Integer[] outdate = new Integer[3];

//        System.out.println("***** INPUT: "+ inputValue);
        Matcher matcher = singleDatePattern.matcher(inputValue);
        if(!matcher.find()) {   // not a single date
            // let's try a date range
            Matcher matcherRange = dateRangePattern.matcher(inputValue);
            if(!matcherRange.find()) {   // not a date range either
                // let's try natty for loose date specification
                Parser dp = new Parser();

                List<DateGroup> grps = dp.parse(inputValue);
                if (grps.size() == 0)
                    throw new IllegalArgumentException("Date " + inputValue + " not recognized by Natty.");

                for (DateGroup grp : grps) {
                    Calendar c1 = new GregorianCalendar();
                    c1.setTime(grp.getDates().get(0));
                    outdate = new Integer[] {
                            c1.get(Calendar.DAY_OF_MONTH)
                            , c1.get(Calendar.MONTH) + 1
                            , c1.get(Calendar.YEAR)};
                    System.out.println(Constants.dateFormat.get().format(grp.getDates().get(0)));
                }
            } else {    // a date range
                Integer[] startdate = extractDateFromMatcher(matcherRange, "day1", "month1", "year1");
                Integer[] enddate = extractDateFromMatcher(matcherRange, "day2", "month2", "year2");
                validateDate(startdate[0], startdate[1], startdate[2]);
                validateDate(enddate[0], enddate[1], enddate[2]);

                outdate = new Integer[] {
                        startdate[0], startdate[1], startdate[2]
                        , enddate[0], enddate[1], enddate[2]
                };
            }
        } else {
            outdate = extractDateFromMatcher(matcher, "day", "month", "year");
            System.out.println(new Gson().toJson(outdate));
            // make some validity checks
            validateDate(outdate[0], outdate[1], outdate[2]);
        }
        for (int i = 0; i < outdate.length; i++) {
            if(outdate[i] == null) outdate[i] = Constants.NODATA_INT;
        }
        return outdate;
    }

    static private void validateDate(Integer day, Integer month, Integer year) throws IllegalArgumentException {
        if(day != null && month != null && year != null) {
            Calendar c = new GregorianCalendar();
            c.setLenient(false);
            c.set(Calendar.DAY_OF_MONTH, day);
            c.set(Calendar.MONTH, month - 1);
            c.set(Calendar.YEAR, year);

            try {
                c.getTime();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date: " + day + "/" + month + "/" + year);
            }
        } else {
            if(day != null && (day < 1 || day > 31)) throw new IllegalArgumentException("Invalid day: " + day);
            if(month != null && (month < 1 || month > 12)) throw new IllegalArgumentException("Invalid month: " + month);
        }
    }

    static private Integer[] extractDateFromMatcher(Matcher matcher, String dayGroup, String monthGroup, String yearGroup) {
        Integer[] out = new Integer[3];
        Integer day = null, month = null, year = null;
        String dayS = matcher.group(dayGroup);
        String monthS = matcher.group(monthGroup);
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
            year = Integer.parseInt(matcher.group(yearGroup));
            if(matcher.group(yearGroup).length() != 4)
                throw new IllegalArgumentException(Messages.getString("error.13", matcher.group()));
        } catch (NumberFormatException e) {
            year = null;
        }
        out[0] = day;
        out[1] = month;
        out[2] = year;
        return out;
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
