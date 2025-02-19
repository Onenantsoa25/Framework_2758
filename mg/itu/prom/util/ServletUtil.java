package mg.itu.prom.util;

import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import mg.itu.prom.annotation.*;


public abstract class ServletUtil {

    public static void dispatchView (HttpServletRequest request, HttpServletResponse response, ModelView modelView) throws ServletException, IOException {
        for (Map.Entry<String, Object> map : modelView.getData().entrySet()) {
            request.setAttribute(map.getKey(), map.getValue());
        }

        RequestDispatcher requestDispatcher = request.getRequestDispatcher(modelView.getUrl());
        requestDispatcher.forward(request, response);
    }

    public static void writeView (HttpServletRequest request, HttpServletResponse response, ModelView modelView) throws ServletException, IOException {
        String json = new Gson().toJson(modelView.getData());
        response.getWriter().write(json);
    }

    public static Map<String, String> extractParameters(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                parameters.put(key, values[0]);
            }
        });
        return parameters;
    }

    public static Object[] getMethodArguments(Method method, Map<String, String> params, HttpServletRequest request) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Param reqParam = parameter.getAnnotation(Param.class);

            if(parameter.getType().equals(MySession.class)) {
                MySession session = new MySession();
                session.setSession(request.getSession());
                arguments[i] = session;
                continue;
            }

            if (reqParam == null) {
                RequestBody reqBody = parameter.getAnnotation(RequestBody.class);
                if (reqBody == null) {
                    throw new IllegalArgumentException("My ETU: 2768 | Cannot match params without annotation in method : " + method.getName());
                }

                Class<?> typeParam = parameter.getType();
                Object paramInstance = instanceObject(typeParam, params);
                arguments[i] = paramInstance;
                continue;
            }

            String paramName = !reqParam.value().isEmpty() ? reqParam.value() : parameter.getName();

            System.out.println("Name = " + paramName);
            String paramValue = params.get(paramName);

            if (paramValue != null) {
                arguments[i] = TypeResolver.castValue(paramValue, parameter.getType());
            } else {
                arguments[i] = null;
                if (isBooleanType(parameter)) {
                    arguments[i] = false;
                }
            }
        }
        return arguments;
    }

    private static  <T> T instanceObject(Class<T> clazz, Map<String, String> params) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T instance = clazz.getDeclaredConstructor().newInstance();

        Field[] attributs = clazz.getDeclaredFields();
        for (Field field : attributs) {
            field.setAccessible(true);

            String valueParams = params.get(field.getName());
            if (valueParams == null) {
                field.set(instance, null);
                continue;
            }
            Object realValue = null;
            try {
                realValue = TypeResolver.castValue(valueParams, field.getType());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            field.set(instance, realValue);
        }

        return instance;
    }

    // public static String getUrl(HashMap<String, Mapping> hashMap, EndPoint endPoint) {
    //     for (Map.Entry<String, Mapping> entry : hashMap.entrySet()) {
    //         if(entry.getValue().getEndPoint().equals(endPoint)) {
    //             return  entry.getKey();
    //         }
    //     }
    //     return "";
    // }

    private static boolean isBooleanType(Parameter parameter) {
        return parameter.getType().equals(boolean.class) || parameter.getType().equals(Boolean.class);
    }

//    public static MySession getMySession(Object obj) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
//        Field[] fields = obj.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            Object value = field.get(obj);
//            if (value instanceof MySession) {
//                return (MySession) value;
//            }
//        }
//        return null;
//    }

    public static void processSession(Object obj, HttpServletRequest request) throws Exception {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(MySession.class)) {
                field.setAccessible(true);
                Object sessionInstance = field.get(obj);
                if (sessionInstance == null) {
                    sessionInstance = MySession.class.getDeclaredConstructor().newInstance();
                    field.set(obj, sessionInstance);
                }

                MySession session = (MySession) sessionInstance;
                session.setSession(request.getSession());
                break;
            }
        }
    }
}
