package pt.floraon.driver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrettyName {
    String value();
    String shortName();
    String[] alias() default {};
    String description() default "[no description]";
    String nameAdvanced() default "";
    String shortNameAdvanced() default "";
    boolean important() default false;
}
