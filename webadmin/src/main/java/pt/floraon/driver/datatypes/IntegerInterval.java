package pt.floraon.driver.datatypes;

import pt.floraon.driver.utils.StringUtils;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegerInterval implements Serializable {
    protected String text;
    private transient static Pattern intervalMatch =
            Pattern.compile("^\\s*(?<modifier>[<>])? *(?<approx>~)? *(?<n1>[0-9]+)(?: *- *(?<n2>[0-9]+))?\\s*$");
    private transient Matcher matcher;
    protected transient Integer minValue, maxValue, exactValue;
    protected transient boolean approximateValue = false, parsed = false;
    protected transient String error;

    public IntegerInterval(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    protected void parseText() {  // lazy parsing
        if(this.parsed) return;
        this.parsed = true;
        if(this.matcher == null) {
            if(StringUtils.isStringEmpty(this.text)) return;
            this.matcher = intervalMatch.matcher(this.text);
            if(!this.matcher.find()) {
                this.error = "Invalid interval: " + this.text;
                return;
            }
            if(this.matcher.group("approx") != null) this.approximateValue = true;

            if(this.matcher.group("n2") == null) {
                if(this.matcher.group("modifier") == null)
                    this.exactValue = Integer.parseInt(this.matcher.group("n1"));
                else {
                    if(this.matcher.group("modifier").equals("<"))
                        this.maxValue = Integer.parseInt(this.matcher.group("n1")) - 1; // exclusive
                    else
                        this.minValue = Integer.parseInt(this.matcher.group("n1")) + 1;
                }
            } else {
                if(this.matcher.group("modifier") != null) {
                    this.error = "Invalid interval: " + this.text;
                    return;
                }
                this.minValue = Integer.parseInt(this.matcher.group("n1"));
                this.maxValue = Integer.parseInt(this.matcher.group("n2"));
            }
        }
    }

    public Integer getMinValue() {
        parseText();
        if(this.minValue != null)
            return this.minValue;
        else {
            if(this.maxValue != null)
                return null;
            else
                return this.exactValue;
        }
    }

    public Integer getMaxValue() {
        parseText();
        if(this.maxValue != null)
            return this.maxValue;
        else {
            if(this.minValue != null)
                return null;
            else
                return this.exactValue;
        }
    }

    public Integer getValue() {
        parseText();
        return this.exactValue;
    }

    public boolean isApproximateValue() {
        parseText();
        return this.approximateValue;
    }

    public String getError() {
        parseText();
        return this.error;
    }

    public boolean isEmpty() {
        parseText();
        return this.exactValue == null && this.maxValue == null && this.minValue == null;
    }

    public boolean overlapsWith(Integer min, Integer max) {
        if(min == null && max == null) return true;

        Integer ev = getValue();

        if(ev != null) {
            if(min != null && max != null && ev >= min && ev <= max) return true;
            if(min == null && ev <= max) return true;
            if(max == null && ev >= min) return true;
            return false;
        }

        Integer maxv = getMaxValue();
        Integer minv = getMinValue();
        if(maxv != null && minv != null) {
            if(min != null && max != null) {
                if(!(min > maxv || max < minv)) return true;
                return false;
            }
            if(min == null && max >= minv) return true;
            if(max == null && min <= maxv) return true;
            return false;
        }

        if(maxv != null) {
            if(min != null && maxv >= min) return true;
            if(min == null) return true;
            return false;
        }


        if(minv != null) {
            if(max != null && minv <= max) return true;
            if(max == null) return true;
        }
        return false;
    }

    public static IntegerInterval emptyInterval() {
        return new IntegerInterval("");
    }
}
