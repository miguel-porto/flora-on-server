package pt.floraon.driver.datatypes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.w3c.dom.traversal.NodeFilter;
import pt.floraon.driver.utils.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Tests if this string has just some words, which are surely not enough for the purpose.
     * @return
     */
    public boolean isAlmostEmpty() {
        return this.toCleanString().length() < 30;
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
        if(this.text == null) return "";
        return Jsoup.parse(this.text).text().replace("\u00a0", " ").trim();
    }

    public String toMarkDownString() {
        String out = this.text.replace("<br>", "\n");
        out = out.replace("&nbsp;", " ");
        Document d = Jsoup.parse(out);
        for(Element el : d.select("span.highlight"))
            el.replaceWith(new TextNode("*"+el.text()+"*"));

        for(Element el : d.select("i"))
            el.replaceWith(new TextNode("*"+el.text()+"*"));

        for(Element el : d.select("span.reference"))
            el.replaceWith(new TextNode("_"+el.text()+"_"));

        for(Element el : d.select("span"))
            el.replaceWith(new TextNode(el.text()));

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
            el.replaceWith(new TextNode(" " + el.text() + " "));

        return d.body().html().replaceAll(" {2,}", " ");
    }

    public int getLength() {
        return this.toCleanString().length();
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

    /**
     * Replaces, recursively in all text nodes, all occurrences of search with replace.
     * Only changes text nodes; keeps element structure unchanged.
     * @param search
     * @param replace
     * @return
     */
    public void replaceSubString(final String search, final String replace) {
        Document d = Jsoup.parse(this.text);
        org.jsoup.select.NodeFilter NF = new org.jsoup.select.NodeFilter() {
            @Override
            public FilterResult head(Node node, int i) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    textNode.text(textNode.text().replaceAll("(?i)" + Pattern.quote(search), replace));
                } else if (node instanceof Element) {
                    if(((Element) node).hasClass("reference"))
                        return FilterResult.SKIP_ENTIRELY;
                }
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int i) {
                return FilterResult.CONTINUE;
            }
        };
        NodeTraversor.filter(NF, d.body());
        this.text = d.body().html();
    }

    public Set<String> replaceSubStringDry(final String search, final String replace) {
        final Set<String> out = new HashSet<>();
        Document d = Jsoup.parse(this.text);
        final Pattern p = Pattern.compile("(?i)\\b\\S*" + Pattern.quote(search) + "\\S*\\b");
        org.jsoup.select.NodeFilter NF = new org.jsoup.select.NodeFilter() {
            @Override
            public FilterResult head(Node node, int i) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    Matcher m = p.matcher(textNode.text());
                    while(m.find()) {
                        out.add(m.group());
                    }
                } else if (node instanceof Element) {
                    if(((Element) node).hasClass("reference"))
                        return FilterResult.SKIP_ENTIRELY;
                }
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int i) {
                return FilterResult.CONTINUE;
            }
        };
        NodeTraversor.filter(NF, d.body());
        return out;
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
