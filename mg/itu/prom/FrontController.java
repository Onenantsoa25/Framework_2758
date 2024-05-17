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
   private boolean test;

   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
        basePackage = config.getInitParameter("base-package");
        test = false;
   }

   public FrontController() {
   }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(test == false){
            try {
                controllers = ScannerClass.scanClasses(basePackage);
                test = true;
            } catch (Exception e) {
                throw new ServletException("Erreur lors du scan des classes", e);
            }
        }
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

}