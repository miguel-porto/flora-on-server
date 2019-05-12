package pt.floraon.bibliography;

import com.google.common.collect.*;
import jline.internal.Log;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pt.floraon.bibliography.entities.Reference;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.Flaggable;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.INodeWorker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A class to process all citations in the text fields of the given list of JavaBeans, assemble a bibliography and
 * rename citations to avoid collisions.
 * It navigates recursively through {@link DiffableBean}. It only collects references from the C class provided
 * TODO: find missing references; allow changing style (style should be configured in this class, not in Reference)
 * @param <T> The class of the beans to process
 * @param <C> The class of the fields from which to collect citations.
 */
public class BibliographyCompiler<T, C> {
    private SetMultimap<String, String> citationMap;
    private List<T> beanList;
    private Iterator<T> beanIterator;
    private Class<C> collectFrom;
    private char[] chars = {'a', 'b', 'c', 'd', 'e', 'f'};
    private IFloraOn driver;

    private class CollectCitations implements CitationProcessor {
        @Override
        public void process(Object bean, String propertyName, Document d, Element el) {
            citationMap.put(el.text(), el.attr("data-id"));
//            System.out.println(el.text()+ ": "+el.attr("data-id"));
        }
    }

    private class ReplaceCitations implements CitationProcessor {
        List<String> idsToReplace;
        String targetId;

        public ReplaceCitations(String[] idsToReplace, String targetId) {
            this.idsToReplace = Arrays.asList(idsToReplace);
            this.targetId = targetId;
        }

        @Override
        public void process(Object bean, String propertyName, Document d, Element el) {
            INodeWorker nwd = driver.getNodeWorkerDriver();
            Reference tmpr;

            try {
                tmpr = nwd.getDocument(driver.asNodeKey(this.targetId), Reference.class);
            } catch (FloraOnException e) {
                e.printStackTrace();
                return;
            }
            if(tmpr == null) {
                jline.internal.Log.error("Reference " + this.targetId + " not found.");
                return;
            }

            if(idsToReplace.contains(el.attr("data-id"))) {
                jline.internal.Log.info("Replacing " + el.text()+ " ("+el.attr("data-id")+") -> " + this.targetId);
                el.attr("data-id", this.targetId);
                el.text("(" + tmpr._getCitation() + ")");
//                setFieldValue(bean, propertyName, ">REPLACED "+this.targetId+"<");
                setFieldValue(bean, propertyName, d.body().html());
                if(Flaggable.class.isAssignableFrom(bean.getClass()))
                    ((Flaggable) bean)._setFlag(true);
            }
        }
    }

    private class MakeUniqueCitations implements CitationProcessor {
        @Override
        public void process(Object bean, String propertyName, Document d, Element el) {
            Set<String> refs = citationMap.get(el.text());
            if(refs == null) {
                System.out.println("ERROR: citation not found: " + el.text());
                return;
            }
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

            setFieldValue(bean, propertyName, d.body().html());
        }
    }

    private void setFieldValue(Object bean, String propertyName, String value) {
        // update the text field with new text
        PropertyUtilsBean propUtils = new PropertyUtilsBean();
        try {   // NOTE: this assumes that the class C has a constructor with one String argument.
            Class<?>[] types = {String.class};
            Constructor<?> constructor = collectFrom.getConstructor(types);
            Object[] parameters = {value};
            C c = (C) constructor.newInstance(parameters);
            propUtils.setProperty(bean, propertyName, c);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
            Log.warn("Error setting property " + propertyName);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    /**
     * A nested citation visitor that does the given action on each visited citation.
     * @param bean The root document
     * @param processor The action to perform
     */
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

/*
    public BibliographyCompiler(List<T> beanList, Class<C> collectFrom, IFloraOn driver) {
        if(beanList.size() == 0) return;
        citationMap = LinkedHashMultimap.create();
        this.beanList = beanList;
        this.collectFrom = collectFrom;
        this.driver = driver;
    }
*/

    public BibliographyCompiler(Iterator<T> beanIterator, Class<C> collectFrom, IFloraOn driver) {
        citationMap = LinkedHashMultimap.create();
        this.beanIterator = beanIterator;
        this.collectFrom = collectFrom;
        this.driver = driver;
    }

    /**
     * Iterates over all eligible fields in the provided documents and collects in-text citations to an internal map.
     */
    public void collectAllCitations() {
        // iterate all text fields in all beans to collect citations
        CitationProcessor collector = new CollectCitations();
        while(beanIterator.hasNext())
            citationVisitor(beanIterator.next(), collector);
    }

    public class BeanIterator implements Iterator<T> {
        private CitationProcessor processor;

        public BeanIterator(CitationProcessor processor) {
            this.processor = processor;
        }

        @Override
        public boolean hasNext() {
            return beanIterator.hasNext();
        }

        @Override
        public T next() {
            T el = beanIterator.next();
            citationVisitor(el, processor);
            return el;
        }

        @Override
        public void remove() {
        }
    }

    /**
     * Appends suffixes to all conflicting in-text citations. This changes the original documents.
     */
    public BeanIterator formatCitations() {
        CitationProcessor formatter = new MakeUniqueCitations();
        return new BeanIterator(formatter);
    }

    /**
     * Replaces all the citations of the given ID array by the ID of replaceWith
     * @param idsToReplace
     * @param replaceWith
     */
    public BeanIterator replaceCitations(String[] idsToReplace, String replaceWith) {
        CitationProcessor replacer = new ReplaceCitations(idsToReplace, replaceWith);
        return new BeanIterator(replacer);
    }

    /**
     * Gets the full list of bibliography cited in the provided document list.
     * @return
     * @throws FloraOnException
     */
    public Set<Reference> getBibliography() throws FloraOnException {
        SortedSet<Reference> out = new TreeSet<>();
        INodeWorker nwd = driver.getNodeWorkerDriver();
        INodeKey nk;
        for(Map.Entry<String, Collection<String>> e : this.citationMap.asMap().entrySet()) {
            Set<String> tmp = ((Set<String>) e.getValue());
            if(tmp.size() == 1) {
                try {
                    nk = driver.asNodeKey(tmp.iterator().next());
                } catch(FloraOnException e1) {
                    System.out.println("Citation error: " + e.getKey());
                    continue;
                }
                Reference r = nwd.getDocument(nk, Reference.class);
                if(r != null) out.add(r);
            } else {
                int c = 0;
                for(String s : tmp) {
                    try {
                        nk = driver.asNodeKey(s);
                    } catch(FloraOnException e1) {
                        System.out.println("Citation error: " + e.getKey());
                        continue;
                    }
                    Reference tmpr = nwd.getDocument(nk, Reference.class);
                    if(tmpr == null) continue;
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
