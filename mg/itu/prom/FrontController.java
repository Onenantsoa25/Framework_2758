package mg.itu.prom;

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
import java.lang.reflect.*;

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
            for (Class<?> controller : controllers) {
                System.out.println("Scanning class: " + controller.getName());
                Method[] functions = controller.getDeclaredMethods();
                for (Method function : functions) {
                    if (function.isAnnotationPresent(Get.class)) {
                        Get get = function.getAnnotation(Get.class);
                        methods.put(get.value(), new Mapping(controller.getName(), function.getName(), function));
                        System.out.println("Mapped URL " + get.value() + " to method " + function.getName() + " in class " + controller.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des classes", e);
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
                            out.print(path + " -> " + map.getClassName() + " -> " + map.getMethodName());
                            Class<?> clazz = Class.forName(map.getClassName());
                            Field[] fields = clazz.getDeclaredFields();
                            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                            for(Field field : fields){
                                if(field.getType() == MySession.class){
                                    field.setAccessible(true);
                                    // Object instance = clazz.getDeclaredConstructor().newInstance();
                                    field.set(controllerInstance, new MySession(request.getSession()));
                                }
                            }
                            Method method = map.getMethod();

                            List<Object> listArgs = MethodParameters.parseParameters(request, map.getMethod());
                            Object valueFunction = map.getMethod().invoke(controllerInstance, listArgs.toArray());

                            dispatchResponse(request,response,valueFunction,out);

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
