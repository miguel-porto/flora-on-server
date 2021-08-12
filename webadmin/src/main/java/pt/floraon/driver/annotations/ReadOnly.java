package pt.floraon.driver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates fields that cannot be altered (e.g. UUID, insertion date...) except, if explicitly set, by a user with
 * MODIFY_OCCURENCES privilege
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ReadOnly {
    boolean adminCanEdit() default false;
}
