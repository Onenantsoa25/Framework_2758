package mg.itu.prom.exception;

public class PackageNotFoundException extends Exception {
    
    public PackageNotFoundException(String packageName) {
        super(" Package " + packageName + "not found");
    }
}
