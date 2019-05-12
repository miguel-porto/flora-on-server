package pt.floraon.redlistdata.fieldprocessing;

import java.util.Set;

/**
 * For visiting all fields of the T type within any bean
 * @param <T>
 */
public interface FieldVisitor<T> {
    void process(Object bean, String propertyName, T value);
}
