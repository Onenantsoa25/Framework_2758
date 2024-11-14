package mg.itu.prom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  // L'annotation sera utilisée sur les méthodes
@Retention(RetentionPolicy.RUNTIME)  // L'annotation sera disponible à l'exécution
public @interface Url {
    String value() default "/";  // Valeur par défaut
}
