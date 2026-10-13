package mg.faneva.summermvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mg.faneva.summermvc.mapping.Mapping;
import mg.faneva.summermvc.mapping.UrlMethod;
import java.lang.reflect.Method;

import mg.faneva.summermvc.binding.DataBinder;
import mg.faneva.summermvc.modelview.ModelView;

public class FrontControllerServlet extends HttpServlet {


    private HashMap<UrlMethod, Mapping> routes;



    @Override
    public void init() throws ServletException {

        super.init();

        routes =
            (HashMap<UrlMethod, Mapping>)
            getServletContext()
            .getAttribute("routes");


        if(routes == null){

            throw new ServletException(
                "Routes non initialisées"
            );

        }

    }
    /*
     * Exécute la méthode du contrôleur
     * avec les arguments préparés par le DataBinder.
     */
    private Object executeMethod(
            HttpServletRequest request,
            Mapping mapping) throws Exception {

        Map<Class<?>, Object> beans =
                (Map<Class<?>, Object>)
                getServletContext().getAttribute("beans");

        Object controller = beans.get(mapping.getController());

        if (controller == null) {
            throw new Exception(
                "Controller introuvable : "
                + mapping.getController()
            );
        }

        Method method = mapping.getMethod();

        // Préparation des arguments
        DataBinder binder = new DataBinder();

        Object[] arguments = binder.bind(request, method);

        // Invocation de la méthode avec les arguments
        return method.invoke(controller, arguments);
    }





    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {

        String uri =
                request.getRequestURI();
        String context =
                request.getContextPath();
        String url =
                uri.substring(
                    context.length()
                );
        if(url.startsWith("/")){

            url =
                url.substring(1);

        }
        String httpMethod =
                request.getMethod();



        UrlMethod key =
                new UrlMethod(
                    url,
                    httpMethod
                );
        if(routes.containsKey(key)){


            Mapping mapping =
                    routes.get(key);
            try {

                /*
                 * Exécution du Controller
                 */
                request.setCharacterEncoding("UTF-8");
                Object result = executeMethod(request, mapping);

                /*
                 * SPRINT 6
                 *
                 * Si la méthode possède
                 * l'annotation @Json,
                 * on retourne directement
                 * le résultat sous forme JSON.
                 */
                if(mapping.isJson()){
                    response.setContentType(
                        "application/json;charset=UTF-8"
                    );


                    PrintWriter out =
                            response.getWriter();
                    /*
                     * SPRINT 6 : sérialisation JSON par réflexion.
                     * Gère : null, String, Number, Boolean,
                     * Collection, Map, tableau, POJO (getters/champs).
                     * Remplace le simple out.println(result)
                     * qui affichait le toString() Java.
                     * (Remplacable plus tard par Jackson ObjectMapper).
                     */
                    out.println(
                        toJson(result)
                    );



                    return;

                }

                if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    if (modelView.getUrl() == null || modelView.getUrl().trim().isEmpty()) {
                        throw new ServletException("L'URL de la vue est vide");
                    }

                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    request.getRequestDispatcher(modelView.getUrl())
                            .forward(request, response);
                    return;
                }

                /*
                 * Résultat simple sans @Json ni ModelView :
                 * conserver l'affichage de diagnostic historique.
                 */
                response.setContentType(
                    "text/html;charset=UTF-8"
                );


                PrintWriter out =
                        response.getWriter();



                out.println(
                    "Route trouvee"
                );


                out.println("<br>");


                out.println(
                    mapping.getController()
                );


                out.println("<br>");


                out.println(
                    mapping.getMethod().getName()
                );


            }
            catch(Exception e){

                throw new ServletException(e);

            }

        }
        else{


            response.setContentType(
                "text/html;charset=UTF-8"
            );


            PrintWriter out =
                    response.getWriter();


            out.println(
                "Route inconnue"
            );

        }

    }





    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {

        processRequest(
            request,
            response
        );

    }
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {

        processRequest(
            request,
            response
        );

    }


    /**
     * SPRINT 6 - Sérialisation JSON minimale sans dépendance externe.
     */
    private String toJson(Object obj) throws Exception {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof java.util.Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Object e : ((java.util.Map<?, ?>) obj).entrySet()) {
                java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) e;
                if (!first) sb.append(",");
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }
        if (obj instanceof java.util.Collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (java.util.Collection<?>) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            return sb.append("]").toString();
        }
        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            int len = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(java.lang.reflect.Array.get(obj, i)));
            }
            return sb.append("]").toString();
        }
        // POJO : via getters publics puis champs privés
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (java.lang.reflect.Method m : obj.getClass().getMethods()) {
            if (m.getParameterCount() != 0) continue;
            String name = m.getName();
            String prop = null;
            if (name.startsWith("get") && name.length() > 3 && !name.equals("getClass"))
                prop = Character.toLowerCase(name.charAt(3)) + name.substring(4);
            else if (name.startsWith("is") && name.length() > 2
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class))
                prop = Character.toLowerCase(name.charAt(2)) + name.substring(3);
            if (prop == null) continue;
            try {
                Object val = m.invoke(obj);
                if (!first) sb.append(",");
                sb.append("\"").append(escape(prop)).append("\":").append(toJson(val));
                first = false;
            } catch (Exception ignored) {}
        }
        if (first) {
            // aucun getter : fallback sur champs
            for (java.lang.reflect.Field f : obj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (!first) sb.append(",");
                sb.append("\"").append(escape(f.getName())).append("\":");
                sb.append(toJson(f.get(obj)));
                first = false;
            }
        }
        return sb.append("}").toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

}
