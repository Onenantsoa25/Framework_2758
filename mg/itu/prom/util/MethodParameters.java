package mg.itu.prom.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import mg.itu.prom.annotation.*;

// import src.mg.itu.prom.annotations.Param;

public class MethodParameters {

    private static Object parseValue(String value, Class<?> type) {
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        else {
            throw new IllegalArgumentException("Type de paramètre non supporté: " + type);
        }
    }


    private static void setMultipartFile(Parameter argParameter, HttpServletRequest request, List<Object> values) throws Exception {
        RequestFile requestFile = argParameter.getAnnotation(RequestFile.class);
        String nameFileInput = "";
        if (requestFile == null || requestFile.value().isEmpty()) {
            nameFileInput = argParameter.getName();
        }
        else {
            nameFileInput = requestFile.value();
        }
    
        Part part = request.getPart(nameFileInput);
        if (part == null) {
            values.add(null);
            return;
        }

        if (argParameter.getType().isAssignableFrom(MultiPartFile.class)) {
            Class<?> paramaType = argParameter.getType();
            Constructor<?> constructor = paramaType.getDeclaredConstructor();
            Object o = constructor.newInstance();
        
            MultiPartFile multiPartFile = (MultiPartFile) o;
            multiPartFile.buildInstance(part, "1859");
            values.add(multiPartFile);
        } else {
            throw new Exception("Parameter not valid Exception for File!");
        }
    }


    public static List<Object> parseParameters(HttpServletRequest request, Method method) throws Exception {
        List<Object> parsedArgs = new ArrayList<>();

        for (Parameter arg : method.getParameters()) {
            Param requestParam = arg.getAnnotation(Param.class);
            RequestBody requestBody = arg.getAnnotation(RequestBody.class);

            if (arg.isAnnotationPresent(RequestFile.class)) {
                setMultipartFile(arg, request, parsedArgs);
                continue;
            }

            if (requestParam != null) {
                String annotName = requestParam.value().isEmpty() ? arg.getName() : requestParam.value();
                String value = request.getParameter(annotName);
                Class<?> type = arg.getType();
                Object parsedValue = TypeResolver.castValue(value, type);

                if (MySession.class.isAssignableFrom(type)) {
                    parsedValue = new MySession(request.getSession());
                }
                
                parsedArgs.add(parsedValue);
            } 
            else if (requestBody != null) {
                Class<?> type = arg.getType();

                if (MySession.class.isAssignableFrom(type)) {
                    parsedArgs.add(new MySession(request.getSession()));
                } else {
                    Constructor<?> constructor = type.getDeclaredConstructor();
                    Object obj = constructor.newInstance();

                    for (Field field : type.getDeclaredFields()) {
                        String fieldName = field.getName();
                        String paramValue = request.getParameter(fieldName);
                        if (paramValue != null) {
                            field.setAccessible(true);
                            field.set(obj, TypeResolver.castValue(paramValue, field.getType()));
                        }
                    }
                    parsedArgs.add(obj);
                }
            }
            else{
                Class<?> type = arg.getType();

                if (MySession.class.isAssignableFrom(type)) {
                    parsedArgs.add(new MySession(request.getSession()));
                }
            }
        }

        return parsedArgs;
    }

}
