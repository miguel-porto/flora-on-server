package pt.floraon.driver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldType {
    enum Type {AUTHORS, IMAGE, DATE, COORDINATES, GENERAL}
    Type value() default Type.GENERAL;
}
