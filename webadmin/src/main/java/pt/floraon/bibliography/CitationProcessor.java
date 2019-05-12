package pt.floraon.bibliography;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public interface CitationProcessor {
    void process(Object bean, String propertyName, Document fieldHTML, Element citationElement);
}
