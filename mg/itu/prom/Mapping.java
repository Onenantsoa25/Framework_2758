package mg.itu.prom;

import java.lang.reflect.Method;
import java.util.Map;

import org.entityframework.dev.Metric;
// import org.serverwork.web.dev.annotation.Rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Mapping {
    private String className;
    // private String methodName;
    private VerbMethod verbMethod;
    private Method method;

    public Mapping(String className, VerbMethod verbMethod, Method method) {
        this.className = className;
        this.verbMethod = verbMethod;
        this.method = method;
    }

    public String getClassName() {
        return className;
    }

    public VerbMethod getMethodName() {
        return verbMethod;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isREST(){
        return method.isAnnotationPresent(Rest.class);
    }

    public Object invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            Object obj = Class.forName(className).getDeclaredConstructor().newInstance();

            String reqAccessMethod = request.getMethod();
            String funAccessMethod = method.getName();

            if (!reqAccessMethod.equalsIgnoreCase(funAccessMethod)) {
                response.sendError(404, "Invalid request URI: " + request.getServletPath());
                return null;
            }

            Method method1 = method;
            Map<String, String> params = ServletUtil.extractParameters(request);
            Object[] args = ServletUtil.getMethodArguments(method1, params, request);

            ServletUtil.processSession(obj, request);
            return method1.invoke(obj, args);
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

}
