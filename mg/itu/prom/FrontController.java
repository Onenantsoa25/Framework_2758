package mg.itu.prom;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletConfig;

import java.net.URL;
import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.lang.reflect.Method;

public class FrontController extends HttpServlet {

   private String basePackage;
   private List<Class<?>> controllers = new ArrayList<>();
   private HashMap<String, Mapping> methods = new HashMap<String, Mapping>();
//    private boolean test;

   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
        basePackage = config.getInitParameter("base-package");
        try {
            controllers = ScannerClass.scanClasses(basePackage);
            for(Class<?> controller : controllers){
                Method[] fonctions = controller.getDeclaredMethods();
                for(Method fonction : fonctions){
                    if(fonction.isAnnotationPresent(Get.class)){
                        Get get = fonction.getAnnotation(Get.class);
                        methods.put(get.value(), new Mapping(controller.getName(), fonction.getName()));
                    }
                }
            }
            // test = true;
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des classes", e);
        }
        // test = false;
   }

   public FrontController() {
   }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // if(test == false){
        // }
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String path = requestURI.substring(contextPath.length());
        System.out.println(url);
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        // out.println(" => "+test);
        if(methods.containsKey(path)){
            Mapping map = methods.get(path);
            out.print(path + " -> " + map.getClassName() + " -> " + map.getMethodName());
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

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}