package mg.itu.prom.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.RequestDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.lang.ModuleLayer.Controller;
import java.lang.reflect.*;
import jakarta.servlet.annotation.MultipartConfig;

import com.google.gson.Gson;

import mg.itu.prom.annotation.*;
import mg.itu.prom.util.*;
import mg.itu.prom.exception.*;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 50 // 50 MB
)

public class FrontController extends HttpServlet {

    private String basePackage;
    private List<Class<?>> controllers = new ArrayList<>();
    private HashMap<String, Mapping> methods = new HashMap<>();
    boolean buildException = true;
    // boolean erreurInterne = false;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        basePackage = config.getInitParameter("base-package");
        try {
            controllers = ScannerClass.scanClasses(basePackage);
            System.out.println("Classes found: " + controllers.size());
            if(controllers.size() > 0){
                this.buildException = false;
            }
            initHashMap();
            // for (Class<?> controller : controllers) {
            //     System.out.println("Scanning class: " + controller.getName());
            //     Method[] functions = controller.getDeclaredMethods();
            //     for (Method function : functions) {
            //         if (function.isAnnotationPresent(Get.class)) {
            //             Get get = function.getAnnotation(Get.class);
            //             methods.put(get.value(), new Mapping(controller.getName(), function.getName(), function));
            //             System.out.println("Mapped URL " + get.value() + " to method " + function.getName() + " in class " + controller.getName());
            //         }
            //     }
            // }
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des classes", e);
        }
    }

    protected String getMethod(Method method) {
        if (method.getAnnotation(Get.class) != null) {
            return "GET";
        }      
        else if (method.getAnnotation(Post.class) != null) {
            return "POST";
        }  
        return "GET"; //par defaut si il y a pas de verb
    }

    protected void initHashMap() throws DuplicateUrlException, PackageNotFoundException, Exception {
        List<Class<?>> classes = ScannerClass.scanClasses(basePackage);
        methods = new HashMap<String, Mapping>();

        for (Class<?> class1 : classes) {
            Method[] methodes = class1.getDeclaredMethods();
        
            for (Method method : methodes) {
                if (method.isAnnotationPresent(Url.class)) {
                    String valueAnnotation = method.getAnnotation(Url.class).value();
                    
                    String verb = getMethod(method);
                    if (!methods.containsKey(valueAnnotation)) {

                        ApiRequest api = new ApiRequest(class1, method);
                        Mapping map = new Mapping();
                        map.addRequest(verb, api);

                        this.methods.put(valueAnnotation, map);
                    }
                    else {
                        Mapping map = methods.get(valueAnnotation);

                        if (!map.containsKey(verb)) {
                            map.addRequest(verb, new ApiRequest(class1, method));
                        }
                        else {
                            throw new DuplicateUrlException(valueAnnotation, verb);
                        }
                        
                    }
                }
            }
        }
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                if(this.buildException){
                    // throw new ServletException("probleme base-package");
                }
                else{
                    // erreurInterne = false;
                    StringBuffer url = request.getRequestURL();

                    String path = request.getServletPath();

                    response.setContentType("text/plain");
                    PrintWriter out = response.getWriter();

                    if (methods.containsKey(path)) {
                        Mapping map = methods.get(path);
                        try {
                            map.isValidVerb(request);
                            

                            ApiRequest apiRequest = map.getRequest(request.getMethod());
                            Class<?> class1 = apiRequest.getClass1();
                            // AuthorizationHandler.isAuthorizedGeneric(class1, request, config);
                            Method method = apiRequest.getMethod();

                            Object instance = apiRequest.getClass1().getDeclaredConstructor().newInstance();
                            List<Object> listArgs = MethodParameters.parseParameters(request, method);

                            // BindingResult br = hasErrors(listArgs);
                            // if (br != null) setBackPage(request,response, br, method);
                            
                            ServletUtil.processSession(instance, request);
                            // AuthorizationHandler.isAuthorized(method, request, config);
                            Object valueFunction = method.invoke(instance, listArgs.toArray());
                            
                            Rest restApi = method.getAnnotation(Rest.class);
                            if (restApi != null) {
                                response.setContentType("text/json");
                                doRestApi(valueFunction, response);
                            } else {
                                dispatchResponse(request, response, valueFunction, out);
                            }


                            // out.print(path + " -> " + map.getApiRequests().getClass().getName() + " -> " + map.getApiRequests().getMethod().getName());
                            // Class<?> clazz = map.getRequest(request.getMethod()).getClass();
                            // Field[] fields = clazz.getDeclaredFields();
                            // Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                            // for(Field field : fields){
                            //     if(field.getType() == MySession.class){
                            //         field.setAccessible(true);
                            //         // Object instance = clazz.getDeclaredConstructor().newInstance();
                            //         field.set(controllerInstance, new MySession(request.getSession()));
                            //     }
                            // }
                            // ApiRequest apiRequest = map.getRequest(request.getMethod());

                            // Method method = apiRequest.getMethod();

                            // List<Object> listArgs = MethodParameters.parseParameters(request, method);
                            // Object valueFunction = map.getRequest(request.getMethod()).getMethod().invoke(controllerInstance, listArgs.toArray());

                            // dispatchResponse(request,response,valueFunction,out);



                            // Object result = method.invoke(controllerInstance);
                            // if (result != null) {
                            //     dispatchResponse(request, response, result, out);
                            // }
                            // if(erreurInterne){
                            //     throw new ServletException("Erreur: Type de retour de la fonction de l'url");
                            // }
                        } catch (ClassNotFoundException e) {
                            out.print("\nClass not found: " + e.getMessage());
                        } catch (NoSuchMethodException e) {
                            out.print("\nNo such method: " + e.getMessage());
                        } catch(ServletException e){
                            throw e;   
                        } catch (Exception e) {
                            out.print("Error executing method: " + e.getMessage());
                            e.printStackTrace(out);
                        }
                    } else {
                        throw new ServletException("Tsy misy ilay method amin'ny url");
                    }
                }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doRestApi(Object valueFunction, HttpServletResponse response) throws Exception {
        try {
            if (valueFunction instanceof ModelView) {
                ModelView modelView = (ModelView) valueFunction;
                HashMap<String, Object> listKeyAndValue = modelView.getData();
                String dataString = JsonParserUtil.objectToJson(listKeyAndValue);
                response.getWriter().println(dataString);
                response.getWriter().close();
            }
            else {
                String dataString = JsonParserUtil.objectToJson(valueFunction);
                response.getWriter().println(dataString);
                response.getWriter().close();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void dispatchResponse(HttpServletRequest request, HttpServletResponse response, Object model, PrintWriter out)
            throws ServletException, IOException {
                // erreurInterne = false;
        if (model instanceof String) {
            out.println(model);
        } else if (model instanceof ModelView) {
            ModelView modelView = (ModelView) model;
            RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
            HashMap<String, Object> data = modelView.getData();
            for (String varName : data.keySet()) {
                request.setAttribute(varName, data.get(varName));
            }
            dispatcher.forward(request, response);
        } else {
            // erreurInterne = true;
            // out.println("return type not found!!");
            throw new ServletException("return type not found!!");
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
