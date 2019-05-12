package pt.floraon.driver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks fields that are not subject to automatic processing tasks.
 * See {@link pt.floraon.redlistdata.fieldprocessing.FieldProcessor}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NoAutomaticProcessing {
}
