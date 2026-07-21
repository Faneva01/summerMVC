package mg.faneva.summermvc.modelview;

import java.util.HashMap;
import java.util.Map;

public class ModelView {

    private String url;

    private Map<String, Object> data;

    public ModelView() {
        data = new HashMap<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void addObject(String key, Object value) {
        data.put(key, value);
    }

}