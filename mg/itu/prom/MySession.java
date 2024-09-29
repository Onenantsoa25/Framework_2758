package mg.itu.prom;

import jakarta.servlet.http.HttpSession;

public class MySession {

    private HttpSession session;

    public MySession(HttpSession session) {
        this.session = session;
    }

    public void setSession(HttpSession session){
        this.session = session;
    }

    public MySession(){}

    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }

    public void invalidate() {
        session.invalidate();
    }

    // Méthodes supplémentaires si nécessaire

    public void put(String name, Object value) {
        setAttribute(name, value);
    }

    public Object get(String name) {
        return getAttribute(name);
    }

    public void remove(String name) {
        removeAttribute(name);
    }

    public HttpSession getHttpSession() {
        return session;
    }
}
