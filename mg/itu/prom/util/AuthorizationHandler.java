package mg.itu.prom.util;

import java.lang.reflect.Method;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom.annotation.authorization.AuthorizedRoles;
import mg.itu.prom.annotation.authorization.RequireLogin;
import mg.itu.prom.exception.UnauthorizedException;
import mg.itu.prom.security.AuthManager;

public class AuthorizationHandler {

    public static void isAuthorized(Method method, HttpServletRequest request, ServletConfig context) throws Exception {
        AuthManager authManager = new AuthManager(context);
        
        HttpSession session1 = request.getSession();
        if (session1 == null) {
            throw new UnauthorizedException("Access denied, Login required !!");
        }
        if (method.isAnnotationPresent(RequireLogin.class) 
            && method.isAnnotationPresent(AuthorizedRoles.class)) {
            throw new IllegalArgumentException("Les deux annotations ne peuvent pas presentes en meme dans la methode "+ method.getName());
        }
        if (method.isAnnotationPresent(RequireLogin.class)) {
            if (!authManager.isAuthenticated(session1)) {
                throw new UnauthorizedException("Access denied, Login required");
            }
        } else if (method.isAnnotationPresent(AuthorizedRoles.class)) {
            if (!authManager.isAuthenticated(session1)) {
                throw new UnauthorizedException("Access denied, Login required !");
            }
            String[] roles = method.getAnnotation(AuthorizedRoles.class).roles();
            if (!authManager.hasRoles(session1, roles)) {
                throw new IllegalArgumentException("Access denied, ROLES required !!");
            }
        }
    }

    
}
