package pt.floraon.driver.utils;

import jline.internal.Log;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.converters.*;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.datatypes.*;
import pt.floraon.occurrences.Abundance;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by miguel on 14-02-2017.
 */
public class BeanUtils {
    /**
     * Compares a new version of a java bean with an old version. <b>Null fields in the new version are excluded from the comparison</b>.
     * This function assumes that the two beans are of the <b>same class</b>.
     * @param oldObject
     * @param newObject
     * @return a map with the names of the fields that are different, and their new values.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static Map<String, Object> diffBeans(DiffableBean oldObject, DiffableBean newObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        BeanMap map = new BeanMap(oldObject);
        Map<String, Object> out = new HashMap<>();

        PropertyUtilsBean propUtils = new PropertyUtilsBean();

        for (Object propNameObject : map.keySet()) {
            String propertyName = (String) propNameObject;
            Object property1 = propUtils.getProperty(oldObject, propertyName);
            Object property2 = propUtils.getProperty(newObject, propertyName);
            if(property2 == null) continue;
            if(property2.getClass().isArray() && ((Object[]) property2).length == 0) continue;
            if(List.class.isAssignableFrom(property2.getClass()) && ((List) property2).size() == 0) continue;
            if(property1 != null && DiffableBean.class.isAssignableFrom(property1.getClass())
                    && DiffableBean.class.isAssignableFrom(property2.getClass())) {
                //out.put(propertyName, ((DiffableBean) property1).compareToBean(property2));
                Map<String, Object> tmp = diffBeans((DiffableBean) property1, (DiffableBean) property2);
                if(tmp.size() > 0) out.put(propertyName, tmp);
            } else if (property1 == null || !property1.equals(property2)) {
                if(property1 != null && property1.getClass().isArray() && property2.getClass().isArray()) {
                    if(!Arrays.equals((Object[]) property1, (Object[]) property2)) {
                        out.put(propertyName, property2);
//						System.out.println("> ARRAY: " + propertyName + " is different (oldValue=\"" + property1 + "\", newValue=\"" + property2 + "\")");
                    }
                } else {
                    out.put(propertyName, property2);
//					System.out.println("> " + propertyName + " is different (oldValue=\"" + property1 + "\", newValue=\"" + property2 + "\")");
                }
            }
        }
        return out;
    }

    /**
     * Fills the bean with the default values, by calling all the getters and setting the returned value.
     * @param bean
     * @return
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static DiffableBean fillBeanDefaults(DiffableBean bean) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        BeanMap map = new BeanMap(bean);
        PropertyUtilsBean propUtils = new PropertyUtilsBean();

        for (Object propNameObject : map.keySet()) {
            String propertyName = (String) propNameObject;
            Object property1 = propUtils.getProperty(bean, propertyName);
            if(property1 == null || !DiffableBean.class.isAssignableFrom(property1.getClass())) {
                if(property1 != null) {
//					System.out.println(propertyName + ": " + property1.toString());
                    org.apache.commons.beanutils.BeanUtils.setProperty(bean, propertyName, property1);
                }
            } else {
//				System.out.println("RECURSIVE: " + propertyName);
                property1 = fillBeanDefaults((DiffableBean) property1);
                org.apache.commons.beanutils.BeanUtils.setProperty(bean, propertyName, property1);
            }
        }
        return bean;
    }

    public static BeanUtilsBean createBeanUtilsNull() {
        BeanUtilsBean out = new BeanUtilsBean();
        IntegerConverter iconverter = new IntegerConverter(null);
        LongConverter longConverter = new LongConverter(null);
        FloatConverter floatConverter = new FloatConverter(null);
        DoubleConverter doubleConverter = new DoubleConverter(null);
        DateConverter dateConverter = new DateConverter(null);
        NumericIntervalConverter numericIntervalConverter = new NumericIntervalConverter(null);
        AbundanceConverter abundanceConverter = new AbundanceConverter(null);
        //ArrayConverter arrayConverter = new ArrayConverter(String[].class, new StringConverter(null));
        ArrayConverter arrayConverter = new ArrayConverter(Integer[].class, iconverter);
        out.getConvertUtils().register(iconverter, Integer.class);
        out.getConvertUtils().register(longConverter, Long.class);
        out.getConvertUtils().register(floatConverter, Float.class);
        out.getConvertUtils().register(doubleConverter, Double.class);
        out.getConvertUtils().register(dateConverter, Date.class);
        out.getConvertUtils().register(arrayConverter, Integer[].class);
        out.getConvertUtils().register(numericIntervalConverter, IntegerInterval.class);
        out.getConvertUtils().register(abundanceConverter, Abundance.class);
        //beanUtilsNull.getConvertUtils().register(arrayConverter, String[].class);
        return out;
    }

    public static BeanUtilsBean createBeanUtilsNullSafeHTML() {
        BeanUtilsBean out = new BeanUtilsBean();
        IntegerConverter iconverter = new IntegerConverter(null);
        LongConverter longConverter = new LongConverter(null);
        ArrayConverter arrayConverter = new ArrayConverter(Integer[].class, iconverter);
        SafeHTMLStringConverter stringConverter = new SafeHTMLStringConverter(null);
        NumericIntervalConverter numericIntervalConverter = new NumericIntervalConverter(null);
        AbundanceConverter abundanceConverter = new AbundanceConverter(null);
        out.getConvertUtils().register(iconverter, Integer.class);
        out.getConvertUtils().register(longConverter, Long.class);
//        beanUtilsNullSafeHTML.getConvertUtils().register(floatConverter, Float.class);
//        beanUtilsNullSafeHTML.getConvertUtils().register(doubleConverter, Double.class);
        out.getConvertUtils().register(arrayConverter, Integer[].class);
        out.getConvertUtils().register(stringConverter, SafeHTMLString.class);
        out.getConvertUtils().register(numericIntervalConverter, IntegerInterval.class);
        out.getConvertUtils().register(abundanceConverter, Abundance.class);
        return out;
    }

    /**
     * Merges (collapses) an array of beans into one by collecting all non-null values and assembling them. If two or more
     * beans have non-null values for a field and they are not equal, an exception is thrown. If they are equal, not a problem.
     * <b>This function does not work with nested beans!</b>
     * @param cls
     * @param beans
     * @param <T>
     * @return The merged bean
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws FloraOnException
     * @throws InstantiationException
     */
    @SafeVarargs
    public static <T extends DiffableBean> T mergeBeans(Class<T> cls, Collection<String> ignoreProperties, String groupingProperty, T... beans) throws IllegalAccessException
            , InvocationTargetException, NoSuchMethodException, FloraOnException, InstantiationException {
        BeanMap propertyMap = new BeanMap(beans[0]);    // we assume beans are all same class! so we take the first as a model
        PropertyUtilsBean propUtils = new PropertyUtilsBean();

        Log.info("Merging "+beans.length+" beans");
        T out = cls.newInstance();

        BeanUtilsBean bub = createBeanUtilsNull();
        if(groupingProperty != null) {      // force grouping by given property, eventually discard data if it conflicts
            Set<String> gP = new HashSet<>();
            for (DiffableBean dB : beans) {
                Object tmp = propUtils.getProperty(dB, groupingProperty);
                if(tmp == null || StringUtils.isStringEmpty(tmp.toString())) {   // different codes, cancel grouping by code
                    groupingProperty = null;
                    Log.info("Cancelling grouping");
                    break;
                } else {
                    gP.add(tmp.toString());
                    if(gP.size() > 1) {    // different codes, cancel grouping by code
                        groupingProperty = null;
                        break;
                    }
                }
            }
        }

        Float sum;
        int count;
        boolean average;
        for (Object propNameObject : propertyMap.keySet()) {
            sum = 0f;
            count = 0;
            average = false;
            String propertyName = (String) propNameObject;
            if ((ignoreProperties != null && ignoreProperties.contains(propertyName))
                    || "class".equals(propertyName)) continue;

            Object property = null, newProperty;

            for (DiffableBean dB : beans) {
                newProperty = propUtils.getProperty(dB, propertyName);

/*
                try {
                    sum += Float.parseFloat(newProperty.toString());
                } catch (Throwable e) {}
*/

                if (newProperty == null) continue;

                if (property == null) {
                    property = newProperty;
                    continue;
                }

                if (property.getClass().isArray()) {
                    if (!Arrays.equals((Object[]) property, (Object[]) newProperty))
                        throw new FloraOnException("Cannot merge beans: field " + propertyName + " has different values.");
                }/* else if(DiffableBean.class.isAssignableFrom(property.getClass())) {
                property = mergeBeans(DiffableBean.class, (DiffableBean) property, (DiffableBean) newProperty);
            }*/ else {  // TODO: nested beans
                    if (!StringUtils.isStringEmpty(property.toString()) && !StringUtils.isStringEmpty(newProperty.toString())
                            && !property.equals(newProperty)) {
                        if(groupingProperty != null) {  // force grouping, discard conflicting field
                            Log.warn("Property conflict <" + propertyName + ">: " + property + " -> " + newProperty + ", but will group anyway, since 'code' has been given.");
//                            average = true;
                        } else {
                            Log.info("Property conflict <" + propertyName + ">: " + property + " -> " + newProperty);
                            throw new FloraOnException("Cannot merge beans: field " + propertyName + " has different values.");
                        }
                    }
                }
                count++;
            }
//            if(average && sum > 0) property = String.format("%.6f", sum / count);
            if (property != null) bub.setProperty(out, propertyName, property);
        }
        return out;
    }

    /**
     * Updates a oldBean with all non-null values in the newBean.
     * @param cls
     * @param ignoreProperties
     * @param oldBean
     * @param newBean
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws FloraOnException
     * @throws InstantiationException
     */
    public static <T extends DiffableBean> T updateBean(Class<T> cls, Collection<String> ignoreProperties, T oldBean, T newBean) throws IllegalAccessException
            , InvocationTargetException, NoSuchMethodException, FloraOnException, InstantiationException {
        BeanMap propertyMap = new BeanMap(oldBean);    // we assume beans are the same class! so we take the first as a model
        PropertyUtilsBean propUtils = new PropertyUtilsBean();
        BeanUtilsBean bub = createBeanUtilsNull();

        T out = cls.getDeclaredConstructor().newInstance();

        for (Object propNameObject : propertyMap.keySet()) {
            String propertyName = (String) propNameObject;
            if((ignoreProperties != null && ignoreProperties.contains(propertyName))
                    || "class".equals(propertyName)) continue;
//            System.out.println("PROP: " + propertyName);
            Object newProperty;

            newProperty = propUtils.getProperty(newBean, propertyName);

            if(newProperty == null) // if null, copy old value
                bub.setProperty(out, propertyName, propUtils.getProperty(oldBean, propertyName));
            else
                bub.setProperty(out, propertyName, newProperty);
            // TODO: nested beans
        }
        return out;
    }
}
