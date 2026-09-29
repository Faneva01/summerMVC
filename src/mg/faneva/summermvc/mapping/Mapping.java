package mg.faneva.summermvc.mapping;

import java.lang.reflect.Method;

public class Mapping {

    private Class<?> controller;

    private Method method;

    private boolean json;

    public Mapping(
            Class<?> controller,
            Method method,
            boolean json) {

        this.controller = controller;
        this.method = method;
        this.json = json;
    }

    public Class<?> getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isJson() {
        return json;
    }
}