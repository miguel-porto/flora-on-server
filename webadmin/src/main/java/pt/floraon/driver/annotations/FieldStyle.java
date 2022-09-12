package pt.floraon.driver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldStyle {
    enum Size {SMALL, BIG, VERYBIG}     // NOTE: VERYBIG only affects inventory summary view
    FieldStyle.Size value() default Size.BIG;
    boolean monospaceFont() default false;
    boolean breakLines() default true;
}
