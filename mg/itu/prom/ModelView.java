package mg.itu.prom;

import java.util.HashMap;

public class ModelView {
    private String Url;
    private HashMap<String,Object> data = new HashMap<>();
    public String getUrl() {
        return Url;
    }
    public void setUrl(String url) {
        Url = url;
    }
    public HashMap<String, Object> getData() {
        return data;
    }
    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
    public void addAttribute(String varName,Object value){
        this.data.put(varName,value);
    }
    public ModelView(String url) {
        Url = url;
    }
    public ModelView() {
    }
}