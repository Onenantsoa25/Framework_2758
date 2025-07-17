package mg.itu.prom.response;

import java.util.Map;

public interface Model extends ResponseHandler {
    Model addObject(String name, Object object);
    Map<String, Object> getModel();
}
