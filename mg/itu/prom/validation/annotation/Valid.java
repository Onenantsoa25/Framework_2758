package mg.itu.prom.validation.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})

public @interface Valid {
    String value() default "";
}
