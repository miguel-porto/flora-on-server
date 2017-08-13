package pt.floraon.bibliography;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.jfree.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.SafeHTMLString;
import pt.floraon.driver.entities.GeneralDBEdge;

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
    private Multimap<String, String> citationMap;
    private List<T> beanList;
    private Class<C> collectFrom;

    private interface CitationProcessor {
        void process(Object bean, String propertyName, Document fieldHTML, Element el);
    }

    private class CollectFromProperties implements CitationProcessor {
        @Override
        public void process(Object bean, String propertyName, Document d, Element el) {
            citationMap.put(el.text(), el.attr("data-id"));
            System.out.println(el.text()+ ": "+el.attr("data-id"));
        }
    }

    private class MakeUniqueCitations implements CitationProcessor {
        @Override
        public void process(Object bean, String propertyName, Document d, Element el) {
            Collection<String> refs = citationMap.get(el.text());
                if(refs.size() == 1)
                    el.text(el.text()+"SOZI");
                else
                    el.text(el.text()+"a");

            PropertyUtilsBean propUtils = new PropertyUtilsBean();


            try {   // NOTE: this assumes that the class C has a constructor with one String argument.
                Class<?>[] types = {String.class};
                Constructor<?> constructor = collectFrom.getConstructor(types);
                Object[] parameters = {d.body().html()};
                C c = (C) constructor.newInstance(parameters);
                propUtils.setProperty(bean, propertyName, c);
                // TODO HERE
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
                e.printStackTrace();
                Log.warn("Error setting property " + propertyName);
                return;
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            System.out.println("CI: " +el.html());
            System.out.println(d.body().html());
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
        citationMap = ArrayListMultimap.create();
        this.beanList = beanList;
        this.collectFrom = collectFrom;

        // iterate all text fields in all beans to collect citations
        CitationProcessor collector = new CollectFromProperties();
        for(T bean : beanList)
            citationVisitor(bean, collector);
    }

    public List<T> formatCitations() {
        CitationProcessor collector = new MakeUniqueCitations();
        for(T bean : beanList)
            citationVisitor(bean, collector);
        return null;
    }

    public Set<String> getBibliography() {
        Set<String> out = new HashSet<>();
        out.addAll(citationMap.values());
        return out;
    }
}
