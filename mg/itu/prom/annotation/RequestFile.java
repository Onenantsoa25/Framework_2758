package mg.itu.prom.annotation;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})

public @interface RequestFile {
    String value() default "";
}

