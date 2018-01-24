package pt.floraon.driver.datatypes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import pt.floraon.driver.utils.StringUtils;

public class SafeHTMLString {
    private String text;

    public SafeHTMLString(String text) {
        if(text != null) {
            if(text.equals(""))
                this.text = "";
            else {
                text = text.replace("\n", "");
                text = text.replace("\r", "");
                text = text.replace("&nbsp;", " ");
                this.text = StringUtils.safeHtmlTagsOnly(text);
            }
        }
    }

    public static SafeHTMLString emptyString() {
        return new SafeHTMLString("");
    }

    public boolean isEmpty() {
        return StringUtils.isStringEmpty(this.toCleanString());
    }

    @Override
    public String toString() {
        return this.text;
    }

    /**
     * Strips all HTML tags and nbsp
     * @return
     */
    public String toCleanString() {
        return Jsoup.parse(this.text).text().replace("\u00a0", " ").trim();
    }

    public String toMarkDownString() {
        String out = this.text.replace("<br>", "\n");
        out = out.replace("&nbsp;", " ");
        Document d = Jsoup.parse(out);
        for(Element el : d.select("span.highlight"))
            el.replaceWith(new TextNode("*"+el.text()+"*", null));

        for(Element el : d.select("i"))
            el.replaceWith(new TextNode("*"+el.text()+"*", null));

        for(Element el : d.select("span.reference"))
            el.replaceWith(new TextNode("_"+el.text()+"_", null));

        for(Element el : d.select("span"))
            el.replaceWith(new TextNode(el.text(), null));

        return d.body().html();
    }

    public String toSimpleHTML() {
        String out = this.text;
        Document d = Jsoup.parse(out);
        for(Element el : d.select("span.highlight"))
            el.replaceWith(new Element("i").text(el.text()));

        for(Element el : d.select("span.reference"))
            el.replaceWith(new Element("cite").text(el.text()));

        for(Element el : d.select("span"))
            el.replaceWith(new TextNode(" " + el.text() + " ", null));

        return d.body().html().replaceAll(" {2,}", " ");
    }

    public int getLength() {
        return StringUtils.cleanText(this.text).length();
    }

    public String searchString(String search) {
        int retlen = 40, start, end;
        String plain = this.toCleanString(), prefix = "", suffix = "";
        int res = plain.toLowerCase().indexOf(search.toLowerCase());
        if(res > -1) {
            res += search.length() / 2;
            if(res - retlen / 2 < 0) start = 0; else {start = res - retlen / 2; prefix = "...";}
            if(start + retlen >= plain.length()) end = plain.length(); else {end = start + retlen; suffix = "...";}
            return prefix + plain.substring(start, end) + suffix;
        } else return null;
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
