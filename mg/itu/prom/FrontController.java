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


public class FrontController extends HttpServlet {

   private String basePackage;
   private List<Class<?>> controllers = new ArrayList<>();
   private int test = 0;

   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      test += 1;
      basePackage = config.getInitParameter("base-package");
      try {
         controllers = scanClasses(basePackage);
      } catch (Exception e) {
         throw new ServletException("Erreur lors du scan des classes", e);
      }
   }

   public FrontController() {
   }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuffer url = request.getRequestURL();
        System.out.println(url);
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println(" => "+test);
        for (Class<?> class1 : controllers) {
            out.println("listes des controllers : ");
            out.println("      =>"+class1.getName());
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

    public static List<Class<?>> scanClasses(String packageName) throws Exception {

        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
    
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new Exception("Package :" + packageName + "nom trouve");
        }
    
        File directory = new File(url.toURI());
        File[] files = directory.listFiles();
    
        for (File file : files) {
            String fileName = file.getName();
            //~ System.out.println("File : " + fileName);

            if (fileName.endsWith(".class")) {
                String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
    
                try {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    Class<?> loadedClass = classLoader.loadClass(className);
                    if (loadedClass.getAnnotation(Controleur.class) != null) {
                        classes.add(loadedClass);   
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return classes;
    }

}
