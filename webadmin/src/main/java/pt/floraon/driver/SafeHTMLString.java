package pt.floraon.driver;

import pt.floraon.driver.utils.StringUtils;

public class SafeHTMLString {
    private String text;

    public SafeHTMLString(String text) {
        if(text != null) {
            if(text.equals(""))
                this.text = "";
            else
                this.text = StringUtils.safeHtmlTagsOnly(text);
        }
    }

    public static SafeHTMLString emptyString() {
        return new SafeHTMLString("");
    }

    @Override
    public String toString() {
        return this.text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SafeHTMLString that = (SafeHTMLString) o;

        return text != null ? text.equals(that.text) : that.text == null;
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }
}
