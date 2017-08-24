package pt.floraon.driver.datatypes;

import pt.floraon.driver.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericInterval {
    private String text;
    private transient static Pattern intervalMatch =
            Pattern.compile("^\\s*(?<modifier>[<>])? *(?<approx>~)? *(?<n1>[0-9]+)(?: *- *(?<n2>[0-9]+))?\\s*$");
    private transient Matcher matcher;
    private transient Integer minValue, maxValue, exactValue;
    private transient boolean approximateValue = false;
    private transient String error;

    public NumericInterval(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    private void parseText() {  // lazy parsing
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
                        this.maxValue = Integer.parseInt(this.matcher.group("n1"));
                    else
                        this.minValue = Integer.parseInt(this.matcher.group("n1"));
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
}
