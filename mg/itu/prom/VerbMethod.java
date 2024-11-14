package mg.itu.prom;

import java.lang.reflect.Method;


public class VerbMethod {
    String verb;
    String method;

    public VerbMethod(){ }

    public VerbMethod(String verbe, String method){
        this.verb = verbe;
        this.method = method;
    }

    public void setVerb(String verb){
        this.verb = verb;
    }

    public void setMethod(String method){
        this.method = method;
    }

    public String getVerb() { return verb; }

    public String getMethod() { return method; }

}
