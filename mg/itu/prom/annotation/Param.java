package mg.itu.prom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // L'annotation sera disponible au moment de l'exécution
@Target(ElementType.PARAMETER) // L'annotation peut être appliquée aux paramètres des méthodes
public @interface Param {
    String value() default "";
}
