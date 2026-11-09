package mg.faneva.summermvc.session;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

/*
 * SPRINT 9
 *
 * Session mise à disposition des méthodes d'action.
 * Le développeur manipule uniquement cette classe :
 * la HttpSession reste cachée dans le framework.
 */
public class MySession {

    private String id;

    private Map<String, Object> data;

    public MySession() {
        data = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void add(String key, Object value) {
        data.put(key, value);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public void clear() {
        data.clear();
    }

    /*
     * Remplit le map à partir de la HttpSession.
     * Appelée par le framework avant la méthode d'action.
     */
    public void load(HttpSession httpSession) {

        id = httpSession.getId();
        data.clear();

        Enumeration<String> names =
                httpSession.getAttributeNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            data.put(name, httpSession.getAttribute(name));
        }
    }

    /*
     * Reporte le contenu du map vers la HttpSession.
     * Appelée par le framework après la méthode d'action.
     */
    public void update(HttpSession httpSession) {

        // Les attributs retirés du map sont retirés de la HttpSession.
        List<String> removed = new ArrayList<>();

        Enumeration<String> names =
                httpSession.getAttributeNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!data.containsKey(name)) {
                removed.add(name);
            }
        }

        for (String name : removed) {
            httpSession.removeAttribute(name);
        }

        // Les attributs ajoutés ou modifiés sont recopiés.
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
    }

}
