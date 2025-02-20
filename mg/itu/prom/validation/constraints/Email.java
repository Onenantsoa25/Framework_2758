package mg.itu.prom.validation.constraints;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

public @interface Email {
    String message() default "Email doit etre valide" ;
}
