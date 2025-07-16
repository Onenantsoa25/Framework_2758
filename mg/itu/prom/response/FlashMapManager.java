package mg.itu.prom.response;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FlashMapManager {
    void saveOutputFlashMap(FlashMap flashMap,HttpServletRequest request, HttpServletResponse response);
    FlashMap retrieveInputFlashMap(HttpServletRequest request, HttpServletResponse response);
}
