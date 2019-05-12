package pt.floraon.redlistdata.fieldprocessing;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.annotations.NoAutomaticProcessing;
import pt.floraon.driver.interfaces.IFloraOn;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FieldProcessor<T, C> implements Iterable<T> {
    private Iterator<T> beanIterator;
    private Class<C> collectFrom;
    private IFloraOn driver;
    private FieldVisitor<C> action;
    private T progress;

    public FieldProcessor(Iterator<T> beanIterator, Class<C> collectFrom, FieldVisitor<C> action, IFloraOn driver) {
        this.beanIterator = beanIterator;
        this.collectFrom = collectFrom;
        this.driver = driver;
        this.action = action;
    }

    public void run() {
        while(beanIterator.hasNext()) {
            this.progress = beanIterator.next();
            fieldVisitor(this.progress, action);
        }
    }

    public T getProgress() {
        return this.progress;
    }

/*
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
*/

    /**
     * A nested field visitor that does the given action on each visited citation.
     * @param bean The root document
     * @param processor The action to perform
     */
    private void fieldVisitor(Object bean, FieldVisitor<C> processor) {
        PropertyUtilsBean propUtils = new PropertyUtilsBean();
        BeanMap propertyMap = new BeanMap(bean);    // beans are all same class! so we take the first as a model
        for (Object propNameObject : propertyMap.keySet()) {
            String propertyName = (String) propNameObject;
            C property;
            Field field = null;
            try {
                // tests whether this field should be skipped from replacement
                // TODO this may only be done once
                field = bean.getClass().getDeclaredField(propertyName);
                if (field != null && field.isAnnotationPresent(NoAutomaticProcessing.class)) continue;
            } catch (NoSuchFieldException e) {
                continue;
            }
            try {
                property = (C) propUtils.getProperty(bean, propertyName);
//                    System.out.println(propertyName+": "+property);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                return;
            }
            if(property == null) continue;

            if(DiffableBean.class.isAssignableFrom(property.getClass()))
                fieldVisitor(property, processor);
            else if(this.collectFrom.isAssignableFrom(property.getClass())) {
                System.out.println("Visiting " + propertyName + " - "+property.getClass());
                processor.process(bean, propertyName, property);
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new BeanIterator(this.action);
    }


    public class BeanIterator implements Iterator<T> {
        private FieldVisitor<C> action;

        BeanIterator(FieldVisitor<C> action) {
            this.action = action;
        }

        @Override
        public boolean hasNext() {
            return beanIterator.hasNext();
        }

        @Override
        public T next() {
            T el = beanIterator.next();
            fieldVisitor(el, action);
            return el;
        }

        @Override
        public void remove() {
        }
    }


}
