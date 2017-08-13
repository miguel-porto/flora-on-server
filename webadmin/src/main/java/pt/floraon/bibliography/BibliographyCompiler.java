package pt.floraon.bibliography;

import com.google.common.collect.*;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.jfree.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pt.floraon.bibliography.entities.Reference;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeWorker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A class to process all citations in the text fields of the given list of JavaBeans, assemble a bibliography and
 * rename citations to avoid collisions.
 * It navigates recursively through {@link DiffableBean}. It only collects references from the C class
 * @param <T> The class of the beans to process
 * @param <C> The class of the fields from which to collect citations.
 */
public class BibliographyCompiler<T, C> {
    private SetMultimap<String, String> citationMap;
    private List<T> beanList;
    private Class<C> collectFrom;
    private char[] chars = {'a', 'b', 'c', 'd', 'e', 'f'};

    private interface CitationProcessor {
        void process(Object bean, String propertyName, Document fieldHTML, Element el);
    }

    private class CollectCitations implements CitationProcessor {
        @Override
        public void process(Object bean, String propertyName, Document d, Element el) {
            citationMap.put(el.text(), el.attr("data-id"));
            System.out.println(el.text()+ ": "+el.attr("data-id"));
        }
    }

    private class MakeUniqueCitations implements CitationProcessor {
        @Override
        public void process(Object bean, String propertyName, Document d, Element el) {
            Set<String> refs = citationMap.get(el.text());
            // add suffix to conflicting citations
            if(refs.size() == 1)
                el.text(el.text());
            else {
                int c = 0;
                for(String s : refs) {
                    if (s.equals(el.attr("data-id"))) break;
                    c++;
                }
                el.text(el.text() + chars[c]);
            }

            // update the text field with new text
            PropertyUtilsBean propUtils = new PropertyUtilsBean();
            try {   // NOTE: this assumes that the class C has a constructor with one String argument.
                Class<?>[] types = {String.class};
                Constructor<?> constructor = collectFrom.getConstructor(types);
                Object[] parameters = {d.body().html()};
                C c = (C) constructor.newInstance(parameters);
                propUtils.setProperty(bean, propertyName, c);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
                e.printStackTrace();
                Log.warn("Error setting property " + propertyName);
                return;
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
/*
            System.out.println("CI: " +el.html());
            System.out.println(d.body().html());
*/
        }
    }

    private void citationVisitor(Object bean, CitationProcessor processor) {
        PropertyUtilsBean propUtils = new PropertyUtilsBean();
        BeanMap propertyMap = new BeanMap(bean);    // beans are all same class! so we take the first as a model
        for (Object propNameObject : propertyMap.keySet()) {
            String propertyName = (String) propNameObject;
            Object property;
            try {
                property = propUtils.getProperty(bean, propertyName);
//                    System.out.println(propertyName+": "+property);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                return;
            }
            if(property == null) continue;

            if(DiffableBean.class.isAssignableFrom(property.getClass()))
                citationVisitor(property, processor);
            else if(this.collectFrom.isAssignableFrom(property.getClass())) {
//                System.out.println("Collecting from " + propertyName + " - "+property.getClass());
                Document d = Jsoup.parse(property.toString());
                Elements citations = d.select("span.reference");
                for(Element el : citations)
                    processor.process(bean, propertyName, d, el);
            }
        }
    }

    public BibliographyCompiler(List<T> beanList, Class<C> collectFrom) {
        if(beanList.size() == 0) return;
        citationMap = LinkedHashMultimap.create();
        this.beanList = beanList;
        this.collectFrom = collectFrom;

        // iterate all text fields in all beans to collect citations
        CitationProcessor collector = new CollectCitations();
        for(T bean : beanList)
            citationVisitor(bean, collector);
    }

    /**
     * Appends suffixes to all conflicting in-text citations. This changes the original documents.
     */
    public void formatCitations() {
        CitationProcessor collector = new MakeUniqueCitations();
        for(T bean : beanList)
            citationVisitor(bean, collector);
    }

    /**
     * Gets the full list of bibliography cited in the provided document list.
     * @param driver The FloraOn driver, as this method needs database access.
     * @return
     * @throws FloraOnException
     */
    public Set<Reference> getBibliography(IFloraOn driver) throws FloraOnException {
        SortedSet<Reference> out = new TreeSet<>();
        INodeWorker nwd = driver.getNodeWorkerDriver();
        for(Map.Entry<String, Collection<String>> e : this.citationMap.asMap().entrySet()) {
            Set<String> tmp = ((Set<String>) e.getValue());
            if(tmp.size() == 1)
                out.add(nwd.getDocument(driver.asNodeKey(tmp.iterator().next()), Reference.class));
            else {
                int c = 0;
                for(String s : tmp) {
                    Reference tmpr = nwd.getDocument(driver.asNodeKey(s), Reference.class);
                    tmpr._setSuffix(chars[c]);
                    out.add(tmpr);
                    c++;
                }
            }
        }
        return out;
/*
        Map<String, Character> out = new HashMap<>();
        out.addAll(citationMap.values());
        return out;
*/
    }
}
