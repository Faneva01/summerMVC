package mg.faneva.summermvc.session;

import java.util.Enumeration;
import java.util.HashMap;
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

    // Contenu de la HttpSession au moment du chargement.
    private Map<String, Object> loaded;

    public MySession() {
        data = new HashMap<>();
        loaded = new HashMap<>();
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

        loaded = new HashMap<>(data);
    }

    /*
     * Reporte vers la HttpSession les changements faits
     * pendant la méthode d'action.
     * Appelée par le framework après la méthode d'action.
     *
     * Seuls les attributs retirés, ajoutés ou modifiés sont reportés :
     * une requête simultanée de la même session ne perd pas
     * ce qu'elle a elle-même enregistré.
     */
    public void update(HttpSession httpSession) {

        // Les attributs retirés du map sont retirés de la HttpSession.
        for (String name : loaded.keySet()) {
            if (!data.containsKey(name)) {
                httpSession.removeAttribute(name);
            }
        }

        // Les attributs ajoutés ou modifiés sont recopiés.
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (!loaded.containsKey(name) || loaded.get(name) != value) {
                httpSession.setAttribute(name, value);
            }
        }

        loaded = new HashMap<>(data);
    }

}
