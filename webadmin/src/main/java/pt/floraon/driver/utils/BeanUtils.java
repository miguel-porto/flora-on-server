package pt.floraon.driver.utils;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.converters.*;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.FloraOnException;

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

    private static BeanUtilsBean beanUtilsNull = new BeanUtilsBean();
    static {
        FloatConverter floatConverter = new FloatConverter(null);
        DoubleConverter doubleConverter = new DoubleConverter(null);
        IntegerConverter iconverter = new IntegerConverter(null);
        LongConverter longConverter = new LongConverter(null);
        //ArrayConverter arrayConverter = new ArrayConverter(String[].class, new StringConverter(null));
        beanUtilsNull.getConvertUtils().register(iconverter, Integer.class);
        beanUtilsNull.getConvertUtils().register(longConverter, Long.class);
        beanUtilsNull.getConvertUtils().register(floatConverter, Float.class);
        beanUtilsNull.getConvertUtils().register(doubleConverter, Double.class);
        //beanUtilsNull.getConvertUtils().register(arrayConverter, String[].class);
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
    public static <T extends DiffableBean> T mergeBeans(Class<T> cls, Collection<String> ignoreProperties, T... beans) throws IllegalAccessException
            , InvocationTargetException, NoSuchMethodException, FloraOnException, InstantiationException {
        BeanMap propertyMap = new BeanMap(beans[0]);    // we assume beans are all same class! so we take the first as a model
        PropertyUtilsBean propUtils = new PropertyUtilsBean();

        T out = cls.newInstance();

        for (Object propNameObject : propertyMap.keySet()) {
            String propertyName = (String) propNameObject;
            if(ignoreProperties.contains(propertyName)) continue;

            Object property = null, newProperty;

            for (DiffableBean dB : beans) {
                newProperty = propUtils.getProperty(dB, propertyName);

                if(newProperty == null) continue;

                if(property == null) {
                    property = newProperty;
                    continue;
                }

                if(property.getClass().isArray()) {
                    if (!Arrays.equals((Object[]) property, (Object[]) newProperty))
                        throw new FloraOnException("Cannot merge beans.");
                }/* else if(DiffableBean.class.isAssignableFrom(property.getClass())) {
                    property = mergeBeans(DiffableBean.class, (DiffableBean) property, (DiffableBean) newProperty);
                }*/ else {  // TODO: nested beans
                    if(!property.equals(newProperty)) throw new FloraOnException("Cannot merge beans.");
                }

            }
            if(property != null) beanUtilsNull.setProperty(out, propertyName, property);
        }
        return out;
    }
}
