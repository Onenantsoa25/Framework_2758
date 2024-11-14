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
import java.lang.reflect.Method;
import com.google.gson.Gson;

import jakarta.servlet.annotation.MultipartConfig;

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

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        basePackage = config.getInitParameter("base-package");
        try {
            // Scan les classes dans le package de base
            controllers = ScannerClass.scanClasses(basePackage);
            System.out.println("Classes found: " + controllers.size());
            if (controllers.size() > 0) {
                this.buildException = false;
            }
            
            for (Class<?> controller : controllers) {
                String baseUrl = "";
                
                // Vérifier si la classe est annotée avec @Url
                if (controller.isAnnotationPresent(Url.class)) {
                    Url urlAnnotation = controller.getAnnotation(Url.class);
                    baseUrl = urlAnnotation.value();
                }
                
                // Scan des méthodes annotées (Get, Post, etc.)
                Method[] functions = controller.getDeclaredMethods();
                for (Method function : functions) {
                    if (function.isAnnotationPresent(Get.class)) {
                        Get get = function.getAnnotation(Get.class);
                        String fullUrl = baseUrl + get.value();
                        methods.put("GET:" + fullUrl, new Mapping(controller.getName(), new VerbMethod("GET", function.getName()), function));
                        System.out.println("Mapped URL " + fullUrl + " [GET] to method " + function.getName() + " in class " + controller.getName());
                    }
                    if (function.isAnnotationPresent(Post.class)) {
                        Post post = function.getAnnotation(Post.class);
                        String fullUrl = baseUrl + post.value();
                        methods.put("POST:" + fullUrl, new Mapping(controller.getName(), new VerbMethod("POST", function.getName()), function));
                        System.out.println("Mapped URL " + fullUrl + " [POST] to method " + function.getName() + " in class " + controller.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des classes", e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (this.buildException) {
            // throw new ServletException("Erreur lors du chargement des mappings.");
            
        }

        String methodType = request.getMethod(); // Le verbe HTTP de la requête (GET, POST, etc.)
        String path = request.getServletPath();  // L'URL demandée

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        // Vérifiez si le chemin est associé à une méthode dans la HashMap
        if (methods.containsKey(path)) {
            Mapping map = methods.get(path);
            
            // Récupérez le verbe attendu pour cette méthode (GET, POST, etc.)
            VerbMethod verbMethod = map.getMethodName(); // Classe contenant le verbe et la méthode associée

            // Vérifiez si le verbe de la requête correspond à celui attendu par la méthode
            if (!methodType.equalsIgnoreCase(verbMethod.getVerb())) {
                // Si le verbe ne correspond pas, renvoyez une erreur
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, 
                                "Le verbe HTTP '" + methodType + "' n'est pas autorisé pour cette URL. Attendu: '" + verbMethod.getVerb() + "'");
                return; // Arrêtez l'exécution
            }

            // Si le verbe correspond, exécutez la méthode normalement
            try {
                out.print(path + " -> " + map.getClassName() + " -> " + verbMethod.getMethod());

                // Récupérer la classe et l'instance de contrôleur associée
                Class<?> clazz = Class.forName(map.getClassName());
                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

                // Exécuter la méthode
                Method method = map.getMethod();
                List<Object> listArgs = MethodParameters.parseParameters(request, map.getMethod());
                Object valueFunction = method.invoke(controllerInstance, listArgs.toArray());

                dispatchResponse(request, response, valueFunction, out);

            } catch (ClassNotFoundException e) {
                out.print("\nClass not found: " + e.getMessage());
            } catch (NoSuchMethodException e) {
                out.print("\nNo such method: " + e.getMessage());
            } catch (Exception e) {
                out.print("Error executing method: " + e.getMessage());
                // e.printStackTrace(out);
            }
        } else {
            // Si l'URL n'est pas trouvée dans la HashMap, renvoyez une erreur
            // response.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucune méthode trouvée pour l'URL " + path);
            out.println("Erreur 404, Aucune methode trouvee pour l'Url: " + path);
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
            // throw new ServletException("Type de retour non géré pour la méthode.");
            out.println("Type de retour non gere");
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
