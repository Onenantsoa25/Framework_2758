package mg.itu.prom;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

public @interface NotBlank {
    String message() default " This field can not be empty ! ";
}

