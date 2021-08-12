package pt.floraon.driver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields that only a user with MODIFY_OCCURENCES privilege can view and edit.
 * This overrides the records that are set as read only (iNaturalist records, for example).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AdminOnly {
}
