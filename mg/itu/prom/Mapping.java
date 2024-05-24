package mg.itu.prom;

public class Mapping{
    String className;
    String methodName;

    public Mapping(String className, String methodName){
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName(){
        return this.className;
    }

    public String getMethodName(){
        return this.methodName;
    }

}