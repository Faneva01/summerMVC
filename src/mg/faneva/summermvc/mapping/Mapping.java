package mg.faneva.summermvc.mapping;

import java.lang.reflect.Method;

public class Mapping {

    private Class<?> controller;

    private Method method;

    private boolean json;

    private boolean auth;

    public Mapping(
            Class<?> controller,
            Method method,
            boolean json,
            boolean auth) {

        this.controller = controller;
        this.method = method;
        this.json = json;
        this.auth = auth;
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

    public boolean isAuth() {
        return auth;
    }
}
