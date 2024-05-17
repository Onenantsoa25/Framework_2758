package mg.itu.prom;

import java.net.URL;
import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class ScannerClass{
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