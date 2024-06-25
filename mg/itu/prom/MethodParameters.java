package mg.itu.prom;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
// import src.mg.itu.Prom16.annotations.Param;

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

    public static List<Object> parseParameters(HttpServletRequest request, Method method) throws Exception {
        List<Object> parsedArgs = new ArrayList<>();


        for (Parameter arg : method.getParameters()) {
            Param requestParam = arg.getAnnotation(Param.class);
            RequestBody requestBody = arg.getAnnotation(RequestBody.class);
            if(requestParam != null){
                String annotName;
                if (!requestParam.value().isEmpty()) {
                    annotName = requestParam.value();
                }
                else {
                    annotName = arg.getName();
                }

                String value = request.getParameter(annotName);
                Class<?> type = arg.getType();
                Object parsedValue = TypeResolver.castValue(value,type);
                parsedArgs.add(parsedValue);
            }
            else if (requestBody != null) {
                try {
                
                    Constructor<?> constructor = arg.getType().getDeclaredConstructor();
                    Object obj = constructor.newInstance();
                    
                    for (Field field : obj.getClass().getDeclaredFields()) {
                        String fieldName = field.getName();
                        String paramValue = request.getParameter(fieldName);
                        if (paramValue != null) {
                            field.setAccessible(true);
                            field.set(obj, TypeResolver.castValue(paramValue, field.getType()));
                        }
                    }
    
                    parsedArgs.add(obj);
                } catch (InstantiationException | IllegalAccessException e) {
                   throw new Exception(e);
                }
            }
            

            

        }
        return parsedArgs;
    }

}
