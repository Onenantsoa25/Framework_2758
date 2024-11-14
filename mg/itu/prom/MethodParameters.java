package mg.itu.prom;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

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
    // validation
    public static void setParamsModel(HttpServletRequest request, Object o, String valParam, boolean haveValidAnnot) throws Exception {
        for (Field atr : o.getClass().getDeclaredFields()) {
            atr.setAccessible(true);
            String val = request.getParameter(valParam + "." + atr.getName());
            Object valeurATr = parseValue(val, atr.getType());
            atr.set(o, valeurATr);
            if (haveValidAnnot) {
                checkValidation(atr, o);
            }
        }
    }

    public static void checkValidation(Field atr, Object o) throws Exception, Exception {
        if (atr.isAnnotationPresent(NotBlank.class)) {
            Object value = atr.get(o);

            if (value instanceof String) {
                String stringValue = (String) value;
                String message = atr.getAnnotation(NotBlank.class).message();

                if (stringValue.trim().isEmpty()) {
                    throw new Exception(message);
                }
            } else {
                throw new Exception(
                    "Le champ annoté avec @NotBlank doit être de type String."
                );
            }
        }
        if (atr.isAnnotationPresent(Min.class)) {
            Object value = atr.get(o);
            Min minAnnot = atr.getAnnotation(Min.class); // Obtenir l'annotation @Min

            if (value instanceof Number) {
                double d = ((Number) value).doubleValue();
                if (d < minAnnot.value()) {
                    throw new Exception(minAnnot.message());
                }
            } else {
                throw new Exception(
                    "Le champ annoté avec @Min doit être de type numérique."
                );
            }
        }
    }
    // end valdation

    public static List<Object> parseParameters(HttpServletRequest request, Method method) throws Exception {
        List<Object> parsedArgs = new ArrayList<>();

        for (Parameter arg : method.getParameters()) {
            
            if (arg.getType().equals(MySession.class)) {
                Object object = MySession.class.getDeclaredConstructor().newInstance();
                MySession session = (MySession) object;
                session.setSession(request.getSession());
                parsedArgs.add(session);
                continue;
            }

            if (arg.isAnnotationPresent(RequestFile.class)) {
                setMultipartFile(arg, request, parsedArgs);
                continue;
            }

            String annotName;
            Object value = null;
            RequestBody requestParam = arg.getAnnotation(RequestBody.class);
            Param modelParam = arg.getAnnotation(Param.class);

            if (modelParam != null) {
                Valid valid = arg.getAnnotation(Valid.class);
                String valueParam = modelParam.value();
                if (valueParam.isEmpty()) {
                    valueParam = arg.getName();
                }

                Class<?> paramaType = arg.getType();
                Constructor<?> constructor = paramaType.getDeclaredConstructor();
                Object o = constructor.newInstance();
                setParamsModel(request, o, valueParam, valid != null); // nouveau
                value = o;
            }
            else if (requestParam != null) {
                if (requestParam.value().isEmpty()) {
                    annotName = arg.getName();
                }
                else {
                    annotName = requestParam.value();
                }
                value = request.getParameter(annotName);
            }
            else {
                throw new Exception("Annotation not found");
            }
            parsedArgs.add(value);
        }
        return parsedArgs;
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

    public static void putSession(HttpServletRequest request, Object obj) throws Exception {
       Field[] fields = obj.getClass().getDeclaredFields();
       
       for (Field field : fields) {
            if (field.getType().equals(MySession.class)) {
                field.setAccessible(true);
                Object object = field.get(obj);

                if (object == null) {
                    object = MySession.class.getDeclaredConstructor().newInstance();
                    field.set(obj, object);
                    MySession session = (MySession) object;
                    session.setSession(request.getSession());
                    break;
                }
            }
       }
    }

}
