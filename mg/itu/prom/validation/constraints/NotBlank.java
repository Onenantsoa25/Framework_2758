package mg.itu.prom.validation.constraints;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

public @interface NotBlank {
    String message() default " This field can not be empty ! ";
}

