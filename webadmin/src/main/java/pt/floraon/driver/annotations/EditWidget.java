package pt.floraon.driver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EditWidget {
    enum Type {CHECKBOX, DROPDOWN, RADIO, TEXT, BIGTEXT, DATE}
    EditWidget.Type value() default Type.TEXT;
    String[] valuesSimple() default {};
    String[] labelsSimple() default {};
    String[] valuesAdvanced() default {};
    String[] labelsAdvanced() default {};
}