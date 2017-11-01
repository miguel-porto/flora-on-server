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
        return StringUtils.isStringEmpty(Jsoup.parse(this.text).text().replace("\u00a0", " ").trim());
    }

    @Override
    public String toString() {
        return this.text;
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
            el.replaceWith(new TextNode(el.text(), null));

        return d.body().html();
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
