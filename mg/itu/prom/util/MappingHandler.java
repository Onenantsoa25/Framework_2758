package mg.itu.prom.util;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import mg.itu.prom.annotation.*;

public class MappingHandler {
    // Liste statique des annotations de mapping
    private static final Class<? extends Annotation> URL_ANNOTATIONS = Url.class;
    public static VerbMapping getUrl(Method method) {
        if (method.isAnnotationPresent(URL_ANNOTATIONS) || method.getDeclaringClass().isAnnotationPresent(URL_ANNOTATIONS)) {
            return VerbMapping.getVerbMapping(method,URL_ANNOTATIONS);
        }   
        return null; // Retourne null si aucune annotation de mapping avec URL n'est trouvée
    }

}
    
